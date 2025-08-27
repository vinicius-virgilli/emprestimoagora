package org.viniciusvirgilli.interceptor;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.viniciusvirgilli.service.RateLimitService;

import java.io.IOException;
import java.util.Map;

/**
 * Interceptor responsável por aplicar rate limiting em todas as requisições HTTP
 * Utiliza o RateLimitService para verificar se as requisições devem ser permitidas
 */
@Slf4j
@Provider
@Priority(1000) // Alta prioridade para executar antes de outros filtros
public class RateLimitInterceptor implements ContainerRequestFilter, ContainerResponseFilter {

    @Inject
    RateLimitService rateLimitService;

    @ConfigProperty(name = "ratelimit.requests.per.second", defaultValue = "550")
    int requestsPerSecond;

    @ConfigProperty(name = "ratelimit.requests.per.minute", defaultValue = "33000")
    int requestsPerMinute;

    @ConfigProperty(name = "ratelimit.requests.per.hour", defaultValue = "1980000")
    int requestsPerHour;

    private static final String RATE_LIMIT_HEADER_REMAINING = "X-RateLimit-Remaining";
    private static final String RATE_LIMIT_HEADER_LIMIT = "X-RateLimit-Limit";
    private static final String RATE_LIMIT_HEADER_RESET = "X-RateLimit-Reset";
    private static final String CLIENT_IP_PROPERTY = "client.ip";
    private static final String ENDPOINT_PROPERTY = "endpoint.path";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Extrair IP do cliente
        String clientIp = extractClientIp(requestContext);
        String endpoint = requestContext.getUriInfo().getPath();
        
        // Armazenar informações no contexto para uso posterior
        requestContext.setProperty(CLIENT_IP_PROPERTY, clientIp);
        requestContext.setProperty(ENDPOINT_PROPERTY, endpoint);
        
        log.debug("Rate limiting check for IP: {} on endpoint: {}", clientIp, endpoint);
        
        // Verificar se a requisição deve ser permitida
        if (!rateLimitService.isRequestAllowed(clientIp, endpoint)) {
            log.warn("Rate limit exceeded for IP: {} on endpoint: {}", clientIp, endpoint);
            
            // Obter estatísticas para headers informativos
            RateLimitService.RateLimitStats stats = rateLimitService.getStats(clientIp);
            
            // Criar resposta de erro 429 Too Many Requests
            Response response = Response.status(429)
                .entity(Map.of(
                    "error", "Rate limit exceeded",
                    "message", "Too many requests. Please try again later.",
                    "requestsInLastSecond", stats.getRequestsInLastSecond(),
                    "requestsInLastMinute", stats.getRequestsInLastMinute(),
                    "requestsInLastHour", stats.getRequestsInLastHour(),
                    "blockedUntil", stats.getBlockedUntil() != null ? stats.getBlockedUntil().toString() : null
                ))
                .header(RATE_LIMIT_HEADER_LIMIT, String.format("%d per second, %d per minute, %d per hour", 
                    requestsPerSecond, requestsPerMinute, requestsPerHour))
                .header(RATE_LIMIT_HEADER_REMAINING, "0")
                .header(RATE_LIMIT_HEADER_RESET, stats.getBlockedUntil() != null ? 
                    stats.getBlockedUntil().toString() : "")
                .build();
            
            requestContext.abortWith(response);
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, 
                      ContainerResponseContext responseContext) throws IOException {
        // Adicionar headers informativos sobre rate limiting na resposta
        String clientIp = (String) requestContext.getProperty(CLIENT_IP_PROPERTY);
        
        if (clientIp != null) {
            RateLimitService.RateLimitStats stats = rateLimitService.getStats(clientIp);
            
            // Calcular requisições restantes para cada período
            int remainingSecond = Math.max(0, requestsPerSecond - stats.getRequestsInLastSecond());
            int remainingMinute = Math.max(0, requestsPerMinute - stats.getRequestsInLastMinute());
            int remainingHour = Math.max(0, requestsPerHour - stats.getRequestsInLastHour());
            
            // O menor valor determina quantas requisições ainda podem ser feitas
            int remaining = Math.min(Math.min(remainingSecond, remainingMinute), remainingHour);
            
            responseContext.getHeaders().add(RATE_LIMIT_HEADER_LIMIT, 
                String.format("%d per second, %d per minute, %d per hour", 
                    requestsPerSecond, requestsPerMinute, requestsPerHour));
            responseContext.getHeaders().add(RATE_LIMIT_HEADER_REMAINING, String.valueOf(remaining));
            
            log.debug("Rate limit headers added for IP: {} - Remaining: {} (sec: {}, min: {}, hour: {})", 
                clientIp, remaining, remainingSecond, remainingMinute, remainingHour);
        }
    }

    /**
     * Extrai o IP do cliente da requisição, considerando proxies e load balancers
     */
    private String extractClientIp(ContainerRequestContext requestContext) {
        // Verificar headers de proxy primeiro
        String xForwardedFor = requestContext.getHeaderString("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For pode conter múltiplos IPs separados por vírgula
            // O primeiro é o IP original do cliente
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = requestContext.getHeaderString("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        String xClientIp = requestContext.getHeaderString("X-Client-IP");
        if (xClientIp != null && !xClientIp.isEmpty()) {
            return xClientIp;
        }
        
        // Fallback para IP da requisição direta
        String remoteAddr = requestContext.getHeaderString("Remote-Addr");
        if (remoteAddr != null && !remoteAddr.isEmpty()) {
            return remoteAddr;
        }
        
        // Último fallback - pode não estar disponível em todos os contextos
        return "unknown";
    }
}
# API Empréstimo Agora

> **Simulador de empréstimos desenvolvido em Java 17 + Quarkus com foco em performance, robustez e facilidade de manutenção**

## 🚀 Execução Rápida

### Opção 1: Docker Compose (Recomendado)
```bash
docker-compose up -d
```

### Opção 2: Execução Local
```bash
# 1. Subir dependências
docker-compose up -d postgres-dev azurite eventhub-simulator

# 2. Executar aplicação
./mvnw quarkus:dev
```

### Opção 3: Build e Execução Manual
```bash
# Build
./mvnw clean package -DskipTests

# Execução
java -jar target/*-runner.jar
```

### 🌐 URLs de Acesso
- **API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/q/swagger-ui
- **Health Check**: http://localhost:8080/q/health
- **Métricas**: http://localhost:8080/q/metrics

---

## 🎯 Implementações Adicionais e Diferenciais

### 🏗️ **Arquitetura Robusta**
- **Framework**: Quarkus 3.15.6 (supersônico e subatômico)
- **Java 17+** com recursos modernos
- **Multi-stage Docker** para builds otimizados
- **Separação clara de responsabilidades** (Controllers, Services, DTOs, Entities)

### ⚡ **Performance e Escalabilidade**
- **Cache Inteligente**: Sistema de cache em memória com TTL configurável
  - Cache de produtos com invalidação automática
  - Cache de listagens com paginação otimizada
  - Estatísticas de cache em tempo real
- **Rate Limiting Avançado**: Controle de requisições por IP
  - 200 req/s, 12.000 req/min, 17.280.000 req/hora
  - Bloqueio temporário inteligente
- **JVM Otimizada**: G1GC, String Deduplication, heap sizing
- **Conexões de BD**: Pool otimizado (min: 2, max: 20)

### 🛡️ **Robustez e Confiabilidade**
- **Validação Completa**: Bean Validation com mensagens customizadas
- **Exception Handling**: Tratamento centralizado de erros
- **Health Checks**: Endpoints de saúde em todos os controllers
- **Transações**: Controle transacional com rollback automático
- **Retry Logic**: Tolerância a falhas com circuit breaker

### 📊 **Observabilidade Completa**
- **OpenTelemetry**: Rastreamento distribuído
- **Métricas Customizadas**: Contadores, timers, histogramas
- **Telemetria Detalhada**: API `/api/telemetria` com métricas por endpoint
- **Logs Estruturados**: Diferentes níveis por ambiente
- **Prometheus**: Métricas exportadas para monitoramento

### 🔄 **Integração e Persistência**
- **Event Hub**: Envio assíncrono para Azure Event Hub
- **Multi-Database**: PostgreSQL (prod), H2 (test), SQL Server (integração)
- **Migrations**: Hibernate com scripts SQL organizados
- **Backup de Dados**: Volumes Docker persistentes

### 🧪 **Qualidade e Testes**
- **Ambiente de Testes**: Configuração H2 in-memory
- **Dados de Teste**: Scripts SQL com cenários completos
- **Postman Collection**: 15+ endpoints documentados
- **Profiles**: dev, test, prod com configurações específicas

### 🔧 **Facilidade de Manutenção**
- **Documentação OpenAPI**: Swagger completo com exemplos
- **DTOs Tipados**: Validação e serialização automática
- **Lombok**: Redução de boilerplate
- **Configuração Externa**: Properties por ambiente
- **Docker Services**: PostgreSQL, Azurite, EventHub Simulator, PgAdmin

---

## 📋 Endpoints Implementados

### 💰 Simulação de Empréstimos
- `POST /api/simulacao/processar` - Processar simulação
- `GET /api/simulacao/{id}` - Buscar simulação por ID
- `GET /api/simulacao/health` - Health check

### 📊 Listagem e Consultas
- `GET /api/simulacoes` - Listar simulações (paginado)
- `GET /api/simulacoes/valores-diarios` - Valores diários
- `GET /api/simulacoes/health` - Health check

### 🏷️ Produtos
- `GET /api/produtos` - Listar produtos
- `GET /api/produtos/{id}` - Buscar produto por ID
- `GET /api/produtos/health` - Health check

### 📈 Volume e Estatísticas
- `GET /api/volume/total-simulacoes` - Total de simulações
- `GET /api/volume/valor-medio-prestacao` - Valor médio de prestação
- `GET /api/volume/health` - Health check

### 🔍 Telemetria e Monitoramento
- `GET /api/telemetria` - Métricas gerais
- `GET /api/telemetria/servico/{nome}` - Métricas por serviço
- `GET /api/telemetria/performance` - Métricas de performance
- `GET /api/telemetria/health` - Health check

---

## 🛠️ Stack Tecnológica

### Core
- **Quarkus 3.15.6** - Framework reativo
- **Java 17** - LTS com recursos modernos
- **Maven** - Gerenciamento de dependências

### Persistência
- **Hibernate ORM + Panache** - ORM simplificado
- **PostgreSQL** - Banco principal
- **H2** - Testes
- **HikariCP** - Pool de conexões

### Observabilidade
- **OpenTelemetry** - Rastreamento distribuído
- **Micrometer + Prometheus** - Métricas
- **SmallRye Health** - Health checks

### Integração
- **Azure Event Hub** - Mensageria
- **Jackson** - Serialização JSON
- **Bean Validation** - Validações

### DevOps
- **Docker + Docker Compose** - Containerização
- **Azurite** - Emulador Azure Storage
- **PgAdmin** - Administração de BD

---

## 🎯 Características de Qualidade

| Aspecto | Implementação |
|---------|---------------|
| **⚡ Performance** | Startup < 1s, Cache hit rate > 90%, Pool otimizado, JVM tuning |
| **🛡️ Robustez** | Rate limiting, Validação multicamada, Exception handling, Transações |
| **🔧 Manutenibilidade** | Código limpo, OpenAPI completa, Logs estruturados, Config externa |
| **🔒 Confiabilidade** | Health checks, Circuit breaker, Persistência transacional, Monitoramento |

---

## 📁 Estrutura do Projeto
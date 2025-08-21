package org.viniciusvirgilli.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.viniciusvirgilli.dto.*;
import org.viniciusvirgilli.entity.Produto;
import org.viniciusvirgilli.enums.TipoSimulacao;
import org.viniciusvirgilli.repository.ProdutoRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Serviço responsável pela lógica de negócio das simulações de empréstimo
 */
@ApplicationScoped
public class SimulacaoService {

    @Inject
    ProdutoRepository produtoRepository;
    
    @Inject
    CacheService cacheService;

    /**
     * Processa uma solicitação de simulação de empréstimo
     * 
     * @param request dados da simulação
     * @return resposta com simulações SAC e PRICE
     */
    @Transactional
    public SimulacaoResponseDTO processarSimulacao(SimulacaoRequestDTO request) {
        // 1. Validar dados de entrada
        validarDadosEntrada(request);
        
        // 2. Buscar produto compatível
        Produto produto = buscarProdutoCompativel(request.getValorDesejado(), request.getPrazo());
        
        // 3. Calcular simulações SAC e PRICE
        List<ResultadoSimulacaoDTO> resultados = calcularSimulacoes(request, produto);
        
        // 4. Gerar ID único para a simulação
        Long idSimulacao = gerarIdSimulacao();
        
        // 5. Montar resposta
        return new SimulacaoResponseDTO(
            idSimulacao,
            produto.codigoProduto,
            produto.nomeProduto,
            produto.percentualTaxaJuros,
            resultados
        );
    }

    /**
     * Valida os dados de entrada da simulação
     */
    private void validarDadosEntrada(SimulacaoRequestDTO request) {
        if (request.getValorDesejado() == null || request.getValorDesejado().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor desejado deve ser maior que zero");
        }
        
        if (request.getPrazo() == null || request.getPrazo() <= 0) {
            throw new IllegalArgumentException("Prazo deve ser maior que zero");
        }
    }

    /**
     * Busca produto compatível com os parâmetros da simulação
     * Utiliza cache com atualização automática a cada minuto
     */
    private Produto buscarProdutoCompativel(BigDecimal valorDesejado, Short prazo) {
        // Buscar produtos que atendem aos critérios usando cache
        List<Produto> produtosCompativeis = cacheService.buscarProdutosCompativeis(
            valorDesejado, prazo
        );
        
        if (produtosCompativeis.isEmpty()) {
            throw new IllegalArgumentException(
                "Nenhum produto disponível para o valor R$ " + valorDesejado + " e prazo de " + prazo + " meses"
            );
        }
        
        // Retornar o produto com menor taxa de juros
        return produtosCompativeis.stream()
            .min((p1, p2) -> p1.percentualTaxaJuros.compareTo(p2.percentualTaxaJuros))
            .orElseThrow(() -> new IllegalArgumentException("Erro ao selecionar produto"));
    }

    /**
     * Calcula as simulações SAC e PRICE
     */
    private List<ResultadoSimulacaoDTO> calcularSimulacoes(SimulacaoRequestDTO request, Produto produto) {
        List<ResultadoSimulacaoDTO> resultados = new ArrayList<>();
        
        // Calcular SAC
        List<ParcelaDTO> parcelasSAC = calcularSAC(
            request.getValorDesejado(), 
            produto.percentualTaxaJuros, 
            request.getPrazo()
        );
        resultados.add(new ResultadoSimulacaoDTO(TipoSimulacao.SAC, parcelasSAC));
        
        // Calcular PRICE
        List<ParcelaDTO> parcelasPRICE = calcularPRICE(
            request.getValorDesejado(), 
            produto.percentualTaxaJuros, 
            request.getPrazo()
        );
        resultados.add(new ResultadoSimulacaoDTO(TipoSimulacao.PRICE, parcelasPRICE));
        
        return resultados;
    }

    /**
     * Calcula as parcelas usando o sistema SAC (Sistema de Amortização Constante)
     */
    private List<ParcelaDTO> calcularSAC(BigDecimal valorEmprestimo, BigDecimal taxaJurosMensal, Short prazoMeses) {
        List<ParcelaDTO> parcelas = new ArrayList<>();
        
        // Amortização constante
        BigDecimal amortizacao = valorEmprestimo.divide(BigDecimal.valueOf(prazoMeses), 9, RoundingMode.HALF_UP);
        
        BigDecimal saldoDevedor = valorEmprestimo;
        
        for (int i = 1; i <= prazoMeses; i++) {
            // Juros sobre o saldo devedor
            BigDecimal juros = saldoDevedor.multiply(taxaJurosMensal).setScale( 9, RoundingMode.HALF_UP);
            
            // Prestação = Amortização + Juros
            BigDecimal prestacao = amortizacao.add(juros);
            
            parcelas.add(new ParcelaDTO(i, amortizacao, juros, prestacao));
            
            // Atualizar saldo devedor
            saldoDevedor = saldoDevedor.subtract(amortizacao);
        }
        
        return parcelas;
    }

    /**
     * Calcula as parcelas usando o sistema PRICE (Sistema Francês)
     */
    private List<ParcelaDTO> calcularPRICE(BigDecimal valorEmprestimo, BigDecimal taxaJurosMensal, Short prazoMeses) {
        List<ParcelaDTO> parcelas = new ArrayList<>();
        
        // Calcular prestação fixa usando fórmula PRICE
        BigDecimal umMaisTaxa = BigDecimal.ONE.add(taxaJurosMensal);
        BigDecimal potencia = umMaisTaxa.pow(prazoMeses);
        BigDecimal coeficiente = taxaJurosMensal.multiply(potencia)
            .divide(potencia.subtract(BigDecimal.ONE), 10, RoundingMode.HALF_UP);
        
        BigDecimal prestacaoFixa = valorEmprestimo.multiply(coeficiente)
            .setScale(9, RoundingMode.HALF_UP);
        
        BigDecimal saldoDevedor = valorEmprestimo;
        
        for (int i = 1; i <= prazoMeses; i++) {
            // Juros sobre o saldo devedor
            BigDecimal juros = saldoDevedor.multiply(taxaJurosMensal)
                .setScale(9, RoundingMode.HALF_UP);
            
            // Amortização = Prestação - Juros
            BigDecimal amortizacao = prestacaoFixa.subtract(juros);
            
            parcelas.add(new ParcelaDTO(i, amortizacao, juros, prestacaoFixa));
            
            // Atualizar saldo devedor
            saldoDevedor = saldoDevedor.subtract(amortizacao);
        }
        
        return parcelas;
    }

    /**
     * Gera um ID único para a simulação
     */
    private Long gerarIdSimulacao() {
        // Usar timestamp + random para gerar ID único
        return System.currentTimeMillis() + (long)(Math.random() * 1000);
    }

    /**
     * Busca produto por código
     */
    public Optional<Produto> buscarProdutoPorCodigo(Integer codigoProduto) {
        return produtoRepository.findByCodigoProduto(codigoProduto);
    }

    /**
     * Lista todos os produtos disponíveis
     */
    public List<Produto> listarTodosProdutos() {
        return produtoRepository.listAll();
    }

    /**
     * Busca produtos por faixa de valor
     */
    public List<Produto> buscarProdutosPorFaixaValor(BigDecimal valorMinimo, BigDecimal valorMaximo) {
        return produtoRepository.findByFaixaValor(valorMinimo, valorMaximo);
    }
}
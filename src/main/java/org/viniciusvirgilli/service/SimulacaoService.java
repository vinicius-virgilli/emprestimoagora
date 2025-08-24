package org.viniciusvirgilli.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.viniciusvirgilli.dto.*;
import org.viniciusvirgilli.model.hack.Produto;
import org.viniciusvirgilli.enums.TipoSimulacao;
import org.viniciusvirgilli.dao.ProdutoDao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Serviço responsável pela lógica de negócio das simulações de empréstimo
 */
@ApplicationScoped
public class SimulacaoService {

    @Inject
    ProdutoDao produtoDao;
    
    @Inject
    ProdutoCacheService produtoCacheService;

    /**
     * Processa uma solicitação de simulação de empréstimo
     * 
     * @param request dados da simulação
     * @return resposta com simulações SAC e PRICE
     */
    @Transactional
    public SimulacaoResponseDTO processarSimulacao(SimulacaoRequestDTO request) {

        validarDadosEntrada(request);

        Produto produto = buscarProdutoCompativel(request.getValorDesejado(), request.getPrazo());

        List<ResultadoSimulacaoDTO> resultados = calcularSimulacoes(request, produto);

        Long idSimulacao = gerarIdSimulacao();

        return new SimulacaoResponseDTO(
            idSimulacao,
            produto.codigoProduto,
            produto.nomeProduto,
            produto.percentualTaxaJuros,
            resultados
        );
    }


    private void validarDadosEntrada(SimulacaoRequestDTO request) {
        if (request.getValorDesejado() == null || request.getValorDesejado().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor desejado deve ser maior que zero");
        }
        
        if (request.getPrazo() == null || request.getPrazo() <= 0) {
            throw new IllegalArgumentException("Prazo deve ser maior que zero");
        }
    }

    private Produto buscarProdutoCompativel(BigDecimal valorDesejado, Short prazo) {
        List<Produto> produtos = produtoCacheService.findAllProdutos();

        return produtos.stream()
                .filter(produto -> valorDesejado.compareTo(produto.getValorMinimo()) >= 0 &&
                                (produto.getValorMaximo() == null || valorDesejado.compareTo(produto.getValorMaximo()) <= 0))
                .filter(produto -> prazo >= produto.getNumeroMinimoMeses() &&
                                (produto.getNumeroMaximoMeses() == null || prazo <= produto.getNumeroMaximoMeses()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Nenhum produto disponível para o valor " + valorDesejado + " e prazo " + prazo));
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
        return produtoDao.findByCodigoProduto(codigoProduto);
    }

    /**
     * Lista todos os produtos disponíveis
     */
    public List<Produto> listarTodosProdutos() {
        return produtoDao.findAll();
    }

    /**
     * Busca produtos por faixa de valor
     */
    public List<Produto> buscarProdutosPorFaixaValor(BigDecimal valorMinimo, BigDecimal valorMaximo) {
        return produtoDao.findByFaixaValor(valorMinimo, valorMaximo);
    }
}
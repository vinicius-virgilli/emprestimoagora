package org.viniciusvirgilli.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.viniciusvirgilli.entity.Produto;
import org.viniciusvirgilli.repository.ProdutoRepository;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Logger;

/**
 * Classe responsável por inicializar dados básicos na aplicação
 * Insere produtos padrão se não existirem no banco de dados
 */
@ApplicationScoped
public class DataInitializer {

    private static final Logger LOGGER = Logger.getLogger(DataInitializer.class.getName());

    @Inject
    ProdutoRepository produtoRepository;

    @ConfigProperty(name = "quarkus.profile")
    String activeProfile;

    /**
     * Método executado na inicialização da aplicação
     * Insere produtos padrão se não existirem e se estiver em perfil de desenvolvimento
     */
    @Transactional
    public void onStart(@Observes StartupEvent ev) {
        LOGGER.info("Iniciando verificação e inserção de dados padrão...");
        LOGGER.info("Perfil ativo: " + activeProfile);
        
        // Só insere dados se estiver em perfil h2 ou postgres (desenvolvimento/teste)
        if (!"h2".equals(activeProfile) && !"postgres".equals(activeProfile)) {
            LOGGER.info("Perfil " + activeProfile + " não permite inserção automática de dados. Pulando inicialização.");
            return;
        }
        
        // Verifica se já existem produtos no banco
        long totalProdutos = produtoRepository.count();
        
        if (totalProdutos == 0) {
            LOGGER.info("Nenhum produto encontrado. Inserindo dados padrão...");
            inserirProdutosPadrao();
            LOGGER.info("Dados padrão inseridos com sucesso!");
        } else {
            LOGGER.info("Produtos já existem no banco de dados. Total: " + totalProdutos);
        }
    }

    /**
     * Insere os produtos padrão no banco de dados carregando do arquivo JSON
     */
    private void inserirProdutosPadrao() {
        try {
            // Carrega o arquivo JSON do classpath
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("produtos-iniciais.json");
            if (inputStream == null) {
                LOGGER.severe("Arquivo produtos-iniciais.json não encontrado no classpath");
                return;
            }

            // Parse do JSON para lista de ProdutoDTO
            ObjectMapper objectMapper = new ObjectMapper();
            List<ProdutoDTO> produtosDTO = objectMapper.readValue(inputStream, new TypeReference<List<ProdutoDTO>>() {});

            // Insere cada produto se não existir
            for (ProdutoDTO produtoDTO : produtosDTO) {
                if (!produtoRepository.existsByCodigoProduto(produtoDTO.getCodigoProduto())) {
                    Produto produto = new Produto(
                        produtoDTO.getCodigoProduto(),
                        produtoDTO.getNomeProduto(),
                        produtoDTO.getPercentualTaxaJuros(),
                        produtoDTO.getNumeroMinimoMeses(),
                        produtoDTO.getNumeroMaximoMeses(),
                        produtoDTO.getValorMinimo(),
                        produtoDTO.getValorMaximo()
                    );
                    produtoRepository.persist(produto);
                    LOGGER.info("Produto inserido: " + produto.nomeProduto + " (ID: " + produto.codigoProduto + ")");
                }
            }

        } catch (Exception e) {
            LOGGER.severe("Erro ao carregar produtos do arquivo JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * DTO para deserialização do JSON
     */
    public static class ProdutoDTO {
        private Integer codigoProduto;
        private String nomeProduto;
        private BigDecimal percentualTaxaJuros;
        private Short numeroMinimoMeses;
        private Short numeroMaximoMeses;
        private BigDecimal valorMinimo;
        private BigDecimal valorMaximo;

        // Getters e Setters
        public Integer getCodigoProduto() { return codigoProduto; }
        public void setCodigoProduto(Integer codigoProduto) { this.codigoProduto = codigoProduto; }

        public String getNomeProduto() { return nomeProduto; }
        public void setNomeProduto(String nomeProduto) { this.nomeProduto = nomeProduto; }

        public BigDecimal getPercentualTaxaJuros() { return percentualTaxaJuros; }
        public void setPercentualTaxaJuros(BigDecimal percentualTaxaJuros) { this.percentualTaxaJuros = percentualTaxaJuros; }

        public Short getNumeroMinimoMeses() { return numeroMinimoMeses; }
        public void setNumeroMinimoMeses(Short numeroMinimoMeses) { this.numeroMinimoMeses = numeroMinimoMeses; }

        public Short getNumeroMaximoMeses() { return numeroMaximoMeses; }
        public void setNumeroMaximoMeses(Short numeroMaximoMeses) { this.numeroMaximoMeses = numeroMaximoMeses; }

        public BigDecimal getValorMinimo() { return valorMinimo; }
        public void setValorMinimo(BigDecimal valorMinimo) { this.valorMinimo = valorMinimo; }

        public BigDecimal getValorMaximo() { return valorMaximo; }
        public void setValorMaximo(BigDecimal valorMaximo) { this.valorMaximo = valorMaximo; }
    }
}
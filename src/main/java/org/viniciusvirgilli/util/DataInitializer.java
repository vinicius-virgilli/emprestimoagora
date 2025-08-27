package org.viniciusvirgilli.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.Getter;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.viniciusvirgilli.model.hack.Produto;
import org.viniciusvirgilli.dao.ProdutoDao;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
public class DataInitializer {

    private static final Logger LOGGER = Logger.getLogger(DataInitializer.class.getName());

    @Inject
    ProdutoDao produtoDao;

    @ConfigProperty(name = "quarkus.profile")
    String activeProfile;

    /**
     * Método executado na inicialização da aplicação
     * Insere produtos padrão se não existirem e se estiver em perfil de desenvolvimento ou de teste
     */
    @Transactional
    public void onStart(@Observes StartupEvent ev) {
        LOGGER.info("Iniciando verificação e inserção de dados padrão...");
        LOGGER.info("Perfil ativo: " + activeProfile);

        if (!"dev".equals(activeProfile) & !"test".equals(activeProfile)) {
            LOGGER.info("Perfil " + activeProfile + " não permite inserção automática de dados. Pulando inicialização.");
            return;
        }
        
        try {
            List<Produto> produtosExistentes = null;
            
            try {
                produtosExistentes = produtoDao.findAll();
            } catch (Exception e) {
                LOGGER.info("Tabela ainda não existe ou não está acessível. Tentando inserir dados padrão...");
            }
            
            if (produtosExistentes == null || produtosExistentes.isEmpty()) {
                LOGGER.info("Nenhum produto encontrado. Inserindo dados padrão...");
                inserirProdutosPadrao();
                LOGGER.info("Dados padrão inseridos com sucesso!");
            } else {
                LOGGER.info("Produtos já existem no banco de dados. Total: " + produtosExistentes.size());
            }
        } catch (Exception e) {
            LOGGER.severe("Erro ao verificar/inserir dados padrão: " + e.getMessage());
        }
    }

    /**
     * Insere os produtos padrão no banco de dados carregando do arquivo JSON
     */
    private void inserirProdutosPadrao() {
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("produtos-iniciais.json");
            if (inputStream == null) {
                LOGGER.severe("Arquivo produtos-iniciais.json não encontrado no classpath");
                return;
            }

            ObjectMapper objectMapper = new ObjectMapper();
            List<ProdutoDTO> produtosDTO = objectMapper.readValue(inputStream, new TypeReference<List<ProdutoDTO>>() {});

            for (ProdutoDTO produtoDTO : produtosDTO) {
                try {
                    Produto produto = new Produto(
                        produtoDTO.getCodigoProduto(),
                        produtoDTO.getNomeProduto(),
                        produtoDTO.getPercentualTaxaJuros(),
                        produtoDTO.getNumeroMinimoMeses(),
                        produtoDTO.getNumeroMaximoMeses(),
                        produtoDTO.getValorMinimo(),
                        produtoDTO.getValorMaximo()
                    );
                    produtoDao.persist(produto);
                    LOGGER.info("Produto inserido: " + produto.nomeProduto + " (ID: " + produto.codigoProduto + ")");
                } catch (Exception e) {
                    LOGGER.warning("Erro ao inserir produto " + produtoDTO.getCodigoProduto() + ": " + e.getMessage());
                }
            }

        } catch (Exception e) {
            LOGGER.severe("Erro ao carregar produtos do arquivo JSON: " + e.getMessage());
        }
    }

    /**
     * DTO para deserialização do JSON
     */
    @Getter
    public static class ProdutoDTO {
        private Integer codigoProduto;
        private String nomeProduto;
        private BigDecimal percentualTaxaJuros;
        private Short numeroMinimoMeses;
        private Short numeroMaximoMeses;
        private BigDecimal valorMinimo;
        private BigDecimal valorMaximo;
    }
}
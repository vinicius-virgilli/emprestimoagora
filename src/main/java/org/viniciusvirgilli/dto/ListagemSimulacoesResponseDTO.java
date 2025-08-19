package org.viniciusvirgilli.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

/**
 * DTO de resposta para listagem paginada de simulações
 */
public class ListagemSimulacoesResponseDTO {

    /**
     * Número da página atual
     */
    @JsonProperty("pagina")
    @NotNull(message = "Número da página é obrigatório")
    @Positive(message = "Número da página deve ser positivo")
    private Integer pagina;

    /**
     * Quantidade total de registros
     */
    @JsonProperty("qtdRegistros")
    @NotNull(message = "Quantidade de registros é obrigatória")
    @PositiveOrZero(message = "Quantidade de registros deve ser positiva ou zero")
    private Integer qtdRegistros;

    /**
     * Quantidade de registros por página
     */
    @JsonProperty("qtdRegistrosPagina")
    @NotNull(message = "Quantidade de registros por página é obrigatória")
    @Positive(message = "Quantidade de registros por página deve ser positiva")
    private Integer qtdRegistrosPagina;

    /**
     * Lista de simulações da página atual
     */
    @JsonProperty("registros")
    @NotNull(message = "Lista de registros é obrigatória")
    @NotEmpty(message = "Lista de registros não pode estar vazia")
    @Valid
    private List<SimulacaoResumoDTO> registros;

    /**
     * Construtor padrão
     */
    public ListagemSimulacoesResponseDTO() {
    }

    /**
     * Construtor completo
     */
    public ListagemSimulacoesResponseDTO(Integer pagina, Integer qtdRegistros, Integer qtdRegistrosPagina,
                                        List<SimulacaoResumoDTO> registros) {
        this.pagina = pagina;
        this.qtdRegistros = qtdRegistros;
        this.qtdRegistrosPagina = qtdRegistrosPagina;
        this.registros = registros;
    }

    // Getters e Setters
    public Integer getPagina() {
        return pagina;
    }

    public void setPagina(Integer pagina) {
        this.pagina = pagina;
    }

    public Integer getQtdRegistros() {
        return qtdRegistros;
    }

    public void setQtdRegistros(Integer qtdRegistros) {
        this.qtdRegistros = qtdRegistros;
    }

    public Integer getQtdRegistrosPagina() {
        return qtdRegistrosPagina;
    }

    public void setQtdRegistrosPagina(Integer qtdRegistrosPagina) {
        this.qtdRegistrosPagina = qtdRegistrosPagina;
    }

    public List<SimulacaoResumoDTO> getRegistros() {
        return registros;
    }

    public void setRegistros(List<SimulacaoResumoDTO> registros) {
        this.registros = registros;
    }

    @Override
    public String toString() {
        return "ListagemSimulacoesResponseDTO{" +
                "pagina=" + pagina +
                ", qtdRegistros=" + qtdRegistros +
                ", qtdRegistrosPagina=" + qtdRegistrosPagina +
                ", registros=" + registros +
                '}';
    }
}
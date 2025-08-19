package org.viniciusvirgilli.enums;

/**
 * Enum para tipos de simulação de empréstimo
 */
public enum TipoSimulacao {
    
    /**
     * Sistema de Amortização Constante (SAC)
     * Amortização fixa, juros decrescentes
     */
    SAC("SAC"),
    
    /**
     * Tabela Price (Sistema Francês)
     * Prestações fixas, amortização crescente
     */
    PRICE("PRICE");
    
    private final String descricao;
    
    TipoSimulacao(String descricao) {
        this.descricao = descricao;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    @Override
    public String toString() {
        return descricao;
    }
}
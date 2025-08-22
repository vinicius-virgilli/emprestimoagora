package org.viniciusvirgilli.model.hack;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;


@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "PRODUTO")
public class Produto {

    @Id
    @Column(name = "CO_PRODUTO")
    @NotNull
    public Integer codigoProduto;

    @Column(name = "NO_PRODUTO", length = 200)
    @NotNull
    @Size(max = 200)
    public String nomeProduto;

    @Column(name = "PC_TAXA_JUROS", precision = 10, scale = 9)
    @NotNull
    public BigDecimal percentualTaxaJuros;

    @Column(name = "NU_MINIMO_MESES")
    @NotNull
    public Short numeroMinimoMeses;

    @Column(name = "NU_MAXIMO_MESES")
    public Short numeroMaximoMeses;

    @Column(name = "VR_MINIMO", precision = 18, scale = 2)
    @NotNull
    public BigDecimal valorMinimo;

    @Column(name = "VR_MAXIMO", precision = 18, scale = 2)
    public BigDecimal valorMaximo;

}
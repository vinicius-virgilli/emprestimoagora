package org.viniciusvirgilli.util;

import lombok.extern.slf4j.Slf4j;
import org.viniciusvirgilli.dto.ParcelaDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CalculadoraUtil {

    /**
     * Calcula o valor total das parcelas
     */
    public static BigDecimal calcularValorTotalDasParcelas(List<ParcelaDTO> parcelas) {

        BigDecimal total = BigDecimal.ZERO;

        if (parcelas != null) {
            for (ParcelaDTO parcela : parcelas) {
                if (parcela.getValorPrestacao() != null) {
                    total = total.add(parcela.getValorPrestacao());
                }
            }
        }

        return total;
    }

    /**
     * Calcula as parcelas usando o sistema SAC (Sistema de Amortização Constante)
     */
    public static List<ParcelaDTO> calcularSAC(BigDecimal valorEmprestimo, BigDecimal taxaJurosMensal, Short prazoMeses) {
        List<ParcelaDTO> parcelas = new ArrayList<>();

        // Amortização constante
        BigDecimal amortizacao = valorEmprestimo.divide(BigDecimal.valueOf(prazoMeses), 9, RoundingMode.HALF_UP);

        BigDecimal saldoDevedor = valorEmprestimo;

        for (int i = 1; i <= prazoMeses; i++) {
            // Juros sobre o saldo devedor
            BigDecimal juros = saldoDevedor.multiply(taxaJurosMensal).setScale(9, RoundingMode.HALF_UP);

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
    public static List<ParcelaDTO> calcularPRICE(BigDecimal valorEmprestimo, BigDecimal taxaJurosMensal, Short prazoMeses) {
        List<ParcelaDTO> parcelas = new ArrayList<>();

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


}
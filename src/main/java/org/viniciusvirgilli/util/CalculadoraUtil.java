package org.viniciusvirgilli.util;

import org.viniciusvirgilli.dto.ParcelaDTO;

import java.math.BigDecimal;
import java.util.List;


public class CalculadoraUtil {

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


}
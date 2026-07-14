package com.BackTecnophones.model.facturacion;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ComprobanteImportes {
	private BigDecimal neto;
	private BigDecimal iva;
	private BigDecimal tributos;
	private BigDecimal exento;
	private BigDecimal noGravado;
	private BigDecimal total;
}

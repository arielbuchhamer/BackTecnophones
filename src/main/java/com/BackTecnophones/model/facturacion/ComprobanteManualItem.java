package com.BackTecnophones.model.facturacion;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ComprobanteManualItem {
	private String descripcion;
	private BigDecimal cantidad;
	private BigDecimal precioUnitario;
}

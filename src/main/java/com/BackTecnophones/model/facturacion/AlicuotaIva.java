package com.BackTecnophones.model.facturacion;

import java.math.BigDecimal;

public enum AlicuotaIva {
	IVA_0(3, "0"),
	IVA_10_5(4, "10.5"),
	IVA_21(5, "21"),
	IVA_27(6, "27"),
	IVA_5(8, "5"),
	IVA_2_5(9, "2.5");

	private final int codigoArca;
	private final BigDecimal porcentaje;

	AlicuotaIva(int codigoArca, String porcentaje) {
		this.codigoArca = codigoArca;
		this.porcentaje = new BigDecimal(porcentaje);
	}

	public int getCodigoArca() {
		return codigoArca;
	}

	public BigDecimal getPorcentaje() {
		return porcentaje;
	}
}

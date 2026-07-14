package com.BackTecnophones.model.facturacion;

public enum TipoComprobante {
	FACTURA_A(1),
	NOTA_DEBITO_A(2),
	NOTA_CREDITO_A(3),
	FACTURA_B(6),
	NOTA_DEBITO_B(7),
	NOTA_CREDITO_B(8),
	FACTURA_C(11),
	NOTA_DEBITO_C(12),
	NOTA_CREDITO_C(13);

	private final int codigoArca;

	TipoComprobante(int codigoArca) {
		this.codigoArca = codigoArca;
	}

	public int getCodigoArca() {
		return codigoArca;
	}
}

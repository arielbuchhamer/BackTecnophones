package com.BackTecnophones.model.facturacion;

public enum TipoDocumento {
	CUIT(80),
	CUIL(86),
	DNI(96),
	PASAPORTE(94),
	CONSUMIDOR_FINAL(99);

	private final int codigoArca;

	TipoDocumento(int codigoArca) {
		this.codigoArca = codigoArca;
	}

	public int getCodigoArca() {
		return codigoArca;
	}
}

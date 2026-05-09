package com.BackTecnophones.model.facturacion;

import lombok.Data;

@Data
public class DatosCliente {
	private String nombre;
	private String apellido;
	private String razonSocial;
	private TipoDocumento tipoDocumento;
	private String numeroDocumento;
	private CondicionIva condicionIva;
	private String domicilio;
	private String email;
}

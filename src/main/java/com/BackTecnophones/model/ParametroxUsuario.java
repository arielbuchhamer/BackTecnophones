package com.BackTecnophones.model;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "parametrosxusuario")
public class ParametroxUsuario extends ClaseGenerica {
	
	public final static int		USUPAR_1 = 1; // Debe la subscripción
	
	private int					userId;
	private int					codParametro;
	private String				valor;

	
}

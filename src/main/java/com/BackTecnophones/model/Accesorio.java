package com.BackTecnophones.model;

import java.math.BigDecimal;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "accesorios")
public class Accesorio  extends ClaseGenerica{
	BigDecimal precio;
	Categoria categoria;
	
	public BigDecimal getPrecio() {
		return precio;
	}
	
	public void setPrecio(BigDecimal precio) {
		this.precio = precio;
	}
	
	public Categoria getCategoria() {
		return categoria;
	}
	
	public void setCategoria(Categoria categoria) {
		this.categoria = categoria;
	}
}

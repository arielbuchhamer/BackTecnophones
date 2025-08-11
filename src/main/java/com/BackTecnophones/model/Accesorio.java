package com.BackTecnophones.model;

import java.math.BigDecimal;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "accesorios")
public class Accesorio  extends ClaseGenerica{
	BigDecimal precio;
	String categoriaId;
	String imageId;
	
	public BigDecimal getPrecio() {
		return precio;
	}
	
	public void setPrecio(BigDecimal precio) {
		this.precio = precio;
	}

	public String getCategoriaId() {
		return categoriaId;
	}

	public void setCategoriaId(String categoriaId) {
		this.categoriaId = categoriaId;
	}

	public String getImageId() {
		return imageId;
	}

	public void setImageId(String imageId) {
		this.imageId = imageId;
	}
	
}

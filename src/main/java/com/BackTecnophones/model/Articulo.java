package com.BackTecnophones.model;

import java.math.BigDecimal;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "articulos")
public class Articulo extends ClaseGenerica {
	BigDecimal precio;
	int stock;
	String imageId;
	String rubroId;
	String categoriaId;
	
	public BigDecimal getPrecio() {
		return precio;
	}
	
	public void setPrecio(BigDecimal precio) {
		this.precio = precio;
	}
	
	public int getStock() {
		return stock;
	}
	
	public void setStock(int stock) {
		this.stock = stock;
	}
	
	public String getImageId() {
		return imageId;
	}
	
	public void setImageId(String imageId) {
		this.imageId = imageId;
	}
	
	public String getRubroId() {
		return rubroId;
	}
	
	public void setRubroId(String rubroId) {
		this.rubroId = rubroId;
	}
	
	public String getCategoriaId() {
		return categoriaId;
	}

	public void setCategoriaId(String categoriaId) {
		this.categoriaId = categoriaId;
	}
}

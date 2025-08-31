package com.BackTecnophones.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "articulos")
public class Articulo extends ClaseGenerica {
	private BigDecimal precio;
	private Integer stock;
	private String imageId;
	private String rubroId;
	private String categoriaId;
	
	private List<Variante> variantes = new ArrayList<>();
	
	public BigDecimal getPrecio() {
		return precio;
	}
	
	public void setPrecio(BigDecimal precio) {
		this.precio = precio;
	}
	
	public int getStock() {
		return stock;
	}
	
	public void setStock(Integer stock) {
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
	
	public List<Variante> getVariantes() {
		return variantes;
	}

	public void setVariantes(List<Variante> variantes) {
		this.variantes = variantes;
	}

	public static class Variante {
	    private String sku;             
	    private String color;
	    private Integer stockVariante;
	    
	    public Variante() {}
	    
		public Variante(String sku, String color, Integer stockVariante) {
			super();
			this.sku = sku;
			this.color = color;
			this.stockVariante = stockVariante;
		}

		public String getSku() {
			return sku;
		}
		
		public void setSku(String sku) {
			this.sku = sku;
		}
		
		public String getColor() {
			return color;
		}
		
		public void setColor(String color) {
			this.color = color;
		}
		
		public Integer getStockVariante() {
			return stockVariante;
		}
		
		public void setStockVariante(Integer stockVariante) {
			this.stockVariante = stockVariante;
		}
	}
}

package com.BackTecnophones.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonProperty;

@Document(collection = "usuarios")
public class Usuario {
	@Id
	private String id;
	private String nombre;
	private String userName;
	private String hashClave;
	@Transient
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // Entra por request, no sale en response
    private String clave;
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getNombre() {
		return nombre;
	}
	
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public String getHashClave() {
		return hashClave;
	}
	
	public void setHashClave(String hashClave) {
		this.hashClave = hashClave;
	}

	public String getClave() {
		return clave;
	}

	public void setClave(String clave) {
		this.clave = clave;
	}
}

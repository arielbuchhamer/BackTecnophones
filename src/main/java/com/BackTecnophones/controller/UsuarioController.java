package com.BackTecnophones.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.BackTecnophones.model.Usuario;
import com.BackTecnophones.service.UsuarioService;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

	@Autowired
	private UsuarioService usuarioService;
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Usuario crearUsuario(@RequestBody Usuario usuario) {
		return usuarioService.save(usuario);
	}
	
	@GetMapping
	public List<Usuario> obtenerUsuarios() {
		return usuarioService.findAll();
	}
	
	@GetMapping("/{id}")
	public Optional<Usuario> usuarioById(@PathVariable String id) {
		return usuarioService.findById(id);
	}
	
	@DeleteMapping("/{id}")
	public void eliminarUsuario(@PathVariable String id) {
		usuarioService.delete(id);
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<Usuario> actualizarUsuario(@PathVariable String id, @RequestBody Usuario usuarioNuevo) {
		Usuario usuarioActualizado = usuarioService.updateUsuario(id, usuarioNuevo);
		
		return ResponseEntity.ok(usuarioActualizado);
	}
}

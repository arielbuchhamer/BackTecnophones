package com.BackTecnophones.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.BackTecnophones.model.Accesorio;
import com.BackTecnophones.service.AccesorioService;

@RestController
@RequestMapping("/accesorios")
public class AccesorioController {

	@Autowired
	private AccesorioService accesorioService;
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Accesorio crearAccesorio(@RequestBody Accesorio accesorio) {
		return accesorioService.save(accesorio);
	}

	@GetMapping
	public List<Accesorio> obtenerAccesorios() {
		return accesorioService.findAll();
	}
	
	@GetMapping("/{id}")
	public Optional<Accesorio> accesorioById(@PathVariable String id) {
		return accesorioService.findById(id);
	}
	
	@DeleteMapping("/{id}")
	public void eliminarAccesorio(@PathVariable String id) {
		accesorioService.delete(id);
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<Accesorio> actualizarAccesorio(@PathVariable String id, @RequestBody Accesorio nuevoAccesorio) {
		Accesorio accesorioActualizado = accesorioService.updateAccesorio(id, nuevoAccesorio);
		
		return ResponseEntity.ok(accesorioActualizado);
	}
}

package com.BackTecnophones.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.BackTecnophones.model.Accesorio;
import com.BackTecnophones.service.AccesorioService;
import com.BackTecnophones.service.CategoriaService;
import com.BackTecnophones.service.ImageService;

@RestController
@RequestMapping("/accesorios")
public class AccesorioController {

	@Autowired
	private AccesorioService accesorioService;
	
	@Autowired
	private CategoriaService categoriaService;
	
	@Autowired
	private ImageService imageService;

	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Accesorio crearAccesorio(@RequestBody Accesorio accesorio) {
		if (accesorio.getCategoriaId() != null)
			categoriaService.findById(accesorio.getCategoriaId())
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria no encontrada"));
		else 
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoria no puede ser nula");
		
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
	
	@PostMapping("/images")
    public Map<String, String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String imageId = imageService.store(file);
            Map<String, String> res = new HashMap<>();
            res.put("id", imageId);
            return res;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error guardando imagen", e);
        }
    }
	
	@GetMapping("/{id}/imagen")
    public ResponseEntity<?> getAccesorioImage(@PathVariable String id) {
        return accesorioService.findById(id).map(acc -> {
            if (acc.getImageId() == null) {
                return ResponseEntity.notFound().build();
            }
            try {
                return imageService.getImage(acc.getImageId());
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error leyendo imagen");
            }
        }).orElse(ResponseEntity.notFound().build());
    }
	
	@PostMapping("/{id}/imagen")
    public Accesorio attachImageToAccesorio(@PathVariable String id, @RequestParam("file") MultipartFile file) {
        try {
            String imageId = imageService.store(file);
            return accesorioService.findById(id).map(acc -> {
                acc.setImageId(imageId);
                return accesorioService.save(acc);
            }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Accesorio no encontrado"));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error subiendo imagen", e);
        }
    }
}

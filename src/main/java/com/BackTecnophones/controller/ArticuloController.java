package com.BackTecnophones.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.BackTecnophones.model.Articulo;
import com.BackTecnophones.service.ArticuloService;
import com.BackTecnophones.service.CategoriaService;
import com.BackTecnophones.service.ImageService;
import com.BackTecnophones.service.RubroService;

@RestController
@RequestMapping("/articulos")
public class ArticuloController {
	
	@Autowired
	private ArticuloService articuloService;
	
	@Autowired
	private CategoriaService categoriaService;
	
	@Autowired
	private RubroService rubroService;
	
	@Autowired
	private ImageService imageService;
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Articulo crearArticulo(@RequestBody Articulo articulo) {
		if (articulo.getRubroId() != null)
			rubroService.findById(articulo.getRubroId())
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rubro no encontrado"));
		else
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rubro no puede ser nulo");
		
		if (articulo.getCategoriaId() != null)
			categoriaService.findById(articulo.getCategoriaId())
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria no encontrada"));
		else 
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoria no puede ser nula");
		
		
		return articuloService.save(articulo);
	}
	
	@GetMapping
	public List<Articulo> obtenerArticulos() {
		return articuloService.findAll();
	}
	
	@GetMapping("/{id}")
	public Articulo articulobyId(@PathVariable String id) {
		return articuloService.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Articulo no encontrado"));
	}
	
	@DeleteMapping("/{id}")
	public void eliminarArticulo(@PathVariable String id) {
		articuloService.delete(id);
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<Articulo> actualizarArticulo(@PathVariable String id, @RequestBody Articulo articuloNuevo) {
		Articulo articuloActualizado = articuloService.updateArticulo(id, articuloNuevo);
		
		return ResponseEntity.ok(articuloActualizado);
	}
	
	@PostMapping("/images")
	public List<Map<String, String>> uploadImages(@RequestParam("files") List<MultipartFile> files) {
	    List<Map<String, String>> response = new ArrayList<>();

	    files.forEach(file -> {
	    	try {
	            String imageId = imageService.store(file);
	            Map<String, String> res = new HashMap<>();
	            res.put("id", imageId);
	            response.add(res);
	        } catch (Exception e) {
	            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error guardando imagen: " + file.getOriginalFilename(), e);
	        }
	    });

	    return response;
	}
	
	@GetMapping("/{id}/imagen")
    public ResponseEntity<?> getAccesorioImage(@PathVariable String id) {
        return articuloService.findById(id).map(acc -> {
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
	
	@PostMapping("/imagenes/obtener-urls")
	public Map<String, String> obtenerUrlsDeImagenes(@RequestBody List<String> idsImagenes) {
	    Map<String, String> urls = new HashMap<>();

	    for (String id : idsImagenes) {
	        try {
	            urls.put(id, imageService.getPublicUrl(id));
	        } catch (Exception e) {
	            urls.put(id, null);
	        }
	    }

	    return urls;
	}
	
	@GetMapping("/images/{imageId}")
	public ResponseEntity<?> obtenerImagenPorId(@PathVariable String imageId) throws IOException {
	    return imageService.getImage(imageId);
	}
	
	@GetMapping("/images/miniatura/{imageId}")
	public ResponseEntity<?> obtenerImgMiniaturaPorId(@PathVariable String imageId) throws IOException {
	    return imageService.obtenerMiniatura(imageId);
	}
	
	@GetMapping("/images/mediana/{imageId}")
	public ResponseEntity<?> obtenerImgMedianaPorId(@PathVariable String imageId) throws IOException {
	    return imageService.obtenerMediana(imageId);
	}
	
	@PostMapping("/{id}/imagen")
    public Articulo attachImageToAccesorio(@PathVariable String id, @RequestParam("file") MultipartFile file) {
        try {
            String imageId = imageService.store(file);
            return articuloService.findById(id).map(art -> {
            	art.setImageId(imageId);
                return articuloService.save(art);
            }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Accesorio no encontrado"));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error subiendo imagen", e);
        }
    }
	
	@GetMapping("/random")
	public List<Articulo> getRandomArticulos(@RequestParam(defaultValue = "12") int limit) {
	    return articuloService.getArticulosRandom(limit);
	}
	
	@GetMapping("/articulosbyrubro/{id}")
	public List<Articulo> getArticulosByRubroId(@PathVariable String id) {
		return articuloService.getArticulosByRubroId(id);
	}
	
	@GetMapping("/completo")
	public List<Articulo> obtenerArticulosCompletos() {
		return articuloService.getArticulosCompletos();
	}
}

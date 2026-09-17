package com.BackTecnophones.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.BackTecnophones.model.Articulo;
import com.BackTecnophones.repository.ArticuloRepository;

// Al arrancar (deploy o reinicio de Railway) procesa en segundo plano las imágenes de todos los artículos,
// así la caché de ImageService ya está llena cuando llega el primer visitante.
@Service
public class PrecargaImagenesService {
	private static final Logger logger = LoggerFactory.getLogger(PrecargaImagenesService.class);

	// Pocos hilos para no dejar sin CPU a los requests que lleguen mientras tanto
	private static final int HILOS = 3;

	private final ArticuloRepository articuloRepo;
	private final ImageService imageService;

	public PrecargaImagenesService(ArticuloRepository articuloRepo, ImageService imageService) {
		this.articuloRepo = articuloRepo;
		this.imageService = imageService;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void precargarAlIniciar() {
		Thread.ofPlatform().name("precarga-imagenes").daemon().start(this::precargarTodas);
	}

	private void precargarTodas() {
		long inicio = System.currentTimeMillis();
		try {
			Set<String> ids = new LinkedHashSet<>();
			for (Articulo art : articuloRepo.findAllByOrderByIdDesc()) {
				if (art.getImageId() != null) ids.add(art.getImageId());
				if (art.getVariantes() == null) continue;
				for (Articulo.Variante v : art.getVariantes()) {
					if (v.getImageVariante() != null) ids.add(v.getImageVariante());
				}
			}

			// Reparte las imágenes entre los hilos
			List<List<String>> partes = new ArrayList<>();
			for (int i = 0; i < HILOS; i++) partes.add(new ArrayList<>());
			int i = 0;
			for (String id : ids) partes.get(i++ % HILOS).add(id);

			try (ExecutorService executor = Executors.newFixedThreadPool(HILOS)) {
				for (List<String> parte : partes) {
					executor.submit(() -> imageService.precargar(parte));
				}
			}

			logger.info("Precarga de {} imágenes terminada en {} ms", ids.size(), System.currentTimeMillis() - inicio);
		} catch (Exception e) {
			logger.warn("Falló la precarga de imágenes: {}", e.getMessage());
		}
	}
}

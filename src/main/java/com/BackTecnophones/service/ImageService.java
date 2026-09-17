package com.BackTecnophones.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.io.ByteArrayInputStream;

import javax.imageio.ImageIO;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.mongodb.client.gridfs.model.GridFSFile;

import net.coobird.thumbnailator.Thumbnails;

@Service
public class ImageService {
	private static final Logger logger = LoggerFactory.getLogger(ImageService.class);

	private final GridFsTemplate gridFsTemplate;

    @Autowired
    public ImageService(GridFsTemplate gridFsTemplate) {
        this.gridFsTemplate = gridFsTemplate;
    }

    private static final long MAX_BYTES_CACHE_IMAGENES = 64L * 1024 * 1024;

    private final Cache<String, ImagenRedimensionada> cacheRedimensionadas = Caffeine.newBuilder()
            .maximumWeight(MAX_BYTES_CACHE_IMAGENES)
            .weigher((String clave, ImagenRedimensionada img) -> img.bytes() == null ? 1 : img.bytes().length)
            .build();
    
    // guarda y devuelve el id (hex) del archivo en GridFS
    public String store(MultipartFile file) throws IOException {
        ObjectId id = gridFsTemplate.store(file.getInputStream(), file.getOriginalFilename(), file.getContentType());
        return id.toHexString();
    }
    
    // obtiene la imagen por imageId y la devuelve como ResponseEntity (para endpoint)
    public ResponseEntity<InputStreamResource> getImage(String imageId) throws IOException {
    	GridFSFile gridFSFile;
        try {
            gridFSFile = gridFsTemplate.findOne(
                Query.query(Criteria.where("_id").is(new ObjectId(imageId)))
            );
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.notFound().build();
        }
        
        if (gridFSFile == null) return ResponseEntity.notFound().build();

        GridFsResource resource = gridFsTemplate.getResource(gridFSFile);

        // Content-Type robusto: si no viene en metadata, infiere por filename
        String ct = resource.getContentType();
        if (ct == null || ct.isBlank()) {
            ct = inferContentType(gridFSFile.getFilename()); // ver helper abajo
        }

        // ETag estable (si md5 no existe en tu driver, arma uno con id|len|fecha)
        String eTag = "\"" + gridFSFile.getObjectId().toHexString()
                + "-" + gridFSFile.getLength()
                + "-" + gridFSFile.getUploadDate().getTime() + "\"";

        // Last-Modified desde uploadDate
        Instant lastModified = gridFSFile.getUploadDate().toInstant();

        InputStreamResource body = new InputStreamResource(resource.getInputStream());
        
        return ResponseEntity.ok()
                .eTag(eTag) // habilita If-None-Match -> 304
                .lastModified(lastModified) // habilita If-Modified-Since -> 304
                .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic().mustRevalidate())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + gridFSFile.getFilename() + "\"")
                .contentLength(gridFSFile.getLength())
                .contentType(MediaType.parseMediaType(ct))
                .body(body);
    }
    
    private String inferContentType(String filename) {
        if (filename == null) return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        String f = filename.toLowerCase();
        if (f.endsWith(".webp")) return "image/webp";
        if (f.endsWith(".avif")) return "image/avif";
        if (f.endsWith(".jpg") || f.endsWith(".jpeg")) return MediaType.IMAGE_JPEG_VALUE;
        if (f.endsWith(".png")) return MediaType.IMAGE_PNG_VALUE;
        if (f.endsWith(".gif")) return MediaType.IMAGE_GIF_VALUE;
        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }
    
    public String getPublicUrl(String imageId) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path("/articulos/images/").path(imageId).toUriString();
    }
    
    // Las versiones redimensionadas se cachean en memoria: procesar la imagen original
    // (bajarla de GridFS, decodificarla y achicarla) cuesta ~1s por imagen, y un imageId
    // nunca cambia de contenido (al reemplazar una imagen se genera un id nuevo).
    public ResponseEntity<InputStreamResource> obtenerMiniatura(String imageId) throws IOException {
        return obtenerRedimensionada(imageId, TamanioImagen.MINIATURA);
    }

    // Para las tarjetas de los listados (se muestran a ~300px)
    public ResponseEntity<InputStreamResource> obtenerTarjeta(String imageId) throws IOException {
        return obtenerRedimensionada(imageId, TamanioImagen.TARJETA);
    }

    // Detalle de producto
    public ResponseEntity<InputStreamResource> obtenerMediana(String imageId) throws IOException {
        return obtenerRedimensionada(imageId, TamanioImagen.MEDIANA);
    }

    private ResponseEntity<InputStreamResource> obtenerRedimensionada(String imageId, TamanioImagen tamanio) throws IOException {
        ObjectId id;
        try {
            id = new ObjectId(imageId);
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.notFound().build();
        }

        ImagenRedimensionada imagen = obtenerDeCache(id, tamanio);

        if (imagen == null) return ResponseEntity.notFound().build();
        if (imagen.bytes() == null) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic().immutable())
                .contentLength(imagen.bytes().length)
                .contentType(MediaType.parseMediaType(imagen.tipoContenido()))
                .body(new InputStreamResource(new ByteArrayInputStream(imagen.bytes())));
    }

    // Genera y cachea los tamaños de las imágenes, para que el primer visitante no espere el procesamiento.
    // Va por tamaño (primero todas las miniaturas, después las tarjetas...) para tener listo antes lo de los listados.
    public void precargar(Collection<String> imageIds) {
        for (TamanioImagen tamanio : TamanioImagen.values()) {
            for (String imageId : imageIds) {
                if (!ObjectId.isValid(imageId)) continue;
                try {
                    obtenerDeCache(new ObjectId(imageId), tamanio);
                } catch (Exception e) {
                    logger.warn("No se pudo precargar la imagen {} ({}): {}", imageId, tamanio, e.getMessage());
                }
            }
        }
    }

    private ImagenRedimensionada obtenerDeCache(ObjectId id, TamanioImagen tamanio) throws IOException {
        try {
            // Caffeine no guarda los null, así que una imagen inexistente no queda cacheada
            return cacheRedimensionadas.get(tamanio.name() + ":" + id.toHexString(), clave -> {
                try {
                    return redimensionar(id, tamanio);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    private ImagenRedimensionada redimensionar(ObjectId id, TamanioImagen tamanio) throws IOException {
        GridFSFile archivoGrid = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(id)));
        if (archivoGrid == null) return null;

        GridFsResource recurso = gridFsTemplate.getResource(archivoGrid);

        String tipoContenido = recurso.getContentType();
        if (tipoContenido == null || tipoContenido.isBlank()) {
            tipoContenido = inferirTipoContenido(archivoGrid.getFilename());
        }

        // Leer imagen original desde GridFS
        BufferedImage original = ImageIO.read(recurso.getInputStream());
        if (original == null) return new ImagenRedimensionada(null, tipoContenido);

        // Thumbnaiator es para el procesamiento de imagenes
        // Redimensionar manteniendo proporciones
        BufferedImage redimensionada = tamanio.altoMaximo == null
                ? Thumbnails.of(original).width(tamanio.anchoMaximo).keepAspectRatio(true).asBufferedImage()
                : Thumbnails.of(original).size(tamanio.anchoMaximo, tamanio.altoMaximo).keepAspectRatio(true).asBufferedImage();

        String formato = convertirTipoAFormato(tipoContenido);

        // Un PNG sin transparencia pesa varias veces más que el mismo JPEG
        if (formato.equals("png") && !redimensionada.getColorModel().hasAlpha()) {
            formato = "jpeg";
            tipoContenido = MediaType.IMAGE_JPEG_VALUE;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(redimensionada, formato, baos);

        return new ImagenRedimensionada(baos.toByteArray(), tipoContenido);
    }

    private enum TamanioImagen {
        MINIATURA(200, null),
        TARJETA(480, 480),
        MEDIANA(1080, 1080); // máximo 1080x1080

        final int anchoMaximo;
        final Integer altoMaximo;

        TamanioImagen(int anchoMaximo, Integer altoMaximo) {
            this.anchoMaximo = anchoMaximo;
            this.altoMaximo = altoMaximo;
        }
    }

    private record ImagenRedimensionada(byte[] bytes, String tipoContenido) {}

    private String inferirTipoContenido(String nombreArchivo) {
        if (nombreArchivo == null) return "image/jpeg";

        nombreArchivo = nombreArchivo.toLowerCase();

        if (nombreArchivo.endsWith(".png"))
            return "image/png";
        if (nombreArchivo.endsWith(".jpg") || nombreArchivo.endsWith(".jpeg"))
            return "image/jpeg";
        if (nombreArchivo.endsWith(".gif"))
            return "image/gif";
        if (nombreArchivo.endsWith(".webp"))
            return "image/webp";

        // Por defecto devolvés jpeg si no reconocés la extensión
        return "image/jpeg";
    }
    
    private String convertirTipoAFormato(String tipo) {
        if (tipo == null) 
        	return "jpeg";
        if (tipo.contains("png")) 
        	return "png";
        if (tipo.contains("gif")) 
        	return "gif";
        if (tipo.contains("webp")) 
        	return "webp";
        return "jpeg";
    }
}

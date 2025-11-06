package com.BackTecnophones.service;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.mongodb.client.gridfs.model.GridFSFile;

@Service
public class ImageService {
	private final GridFsTemplate gridFsTemplate;

    @Autowired
    public ImageService(GridFsTemplate gridFsTemplate) {
        this.gridFsTemplate = gridFsTemplate;
    }
    
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
}

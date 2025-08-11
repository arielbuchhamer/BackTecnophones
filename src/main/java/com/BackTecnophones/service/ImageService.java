package com.BackTecnophones.service;

import java.io.IOException;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
        GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(new ObjectId(imageId))));
        if (gridFSFile == null) {
            return ResponseEntity.notFound().build();
        }
        GridFsResource resource = gridFsTemplate.getResource(gridFSFile);
        String contentType = resource.getContentType() != null ? resource.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        InputStreamResource inputStream = new InputStreamResource(resource.getInputStream());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + gridFSFile.getFilename() + "\"")
                .contentLength(gridFSFile.getLength())
                .contentType(MediaType.parseMediaType(contentType))
                .body(inputStream);
    }
}

package com.example.Storage;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;



@RestController
public class DocumentController {

    private final Logger logger = LoggerFactory.getLogger(DocumentController.class);

    // Save the uploaded file to this folder
    private static Path currentRelativePath = Paths.get("");
    private static String UPLOADED_FOLDER = currentRelativePath.toAbsolutePath().toString() + "/";

    @Autowired
    DocumentService documentService;

    // Single file upload
    @PostMapping("/storage/documents")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile uploadfile) {

        logger.debug("Single file upload!");

        if (uploadfile.isEmpty()) {
            return new ResponseEntity("You must select a file!", HttpStatus.OK);
        }

        Document doc = new Document();
        Document result = null;

        try {

            saveUploadedFiles(Arrays.asList(uploadfile));

            doc.setName(uploadfile.getOriginalFilename());

            result = documentService.saveOrUpdate(doc);

        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity(result.getId(), new HttpHeaders(),
                HttpStatus.CREATED);

    }

    @PutMapping("/storage/documents/{id}")
    public ResponseEntity<?> updateFile(@PathVariable String id, @RequestParam("file") MultipartFile uploadfile) throws IOException {

        Document doc = documentService.getDocumentById(id);
        if (doc == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        doc.setName(uploadfile.getOriginalFilename());
        try {

            saveUploadedFiles(Arrays.asList(uploadfile));

        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);

    }

    // file download
    @GetMapping(path = "/storage/documents/{id}")
    public ResponseEntity<Resource> download(@PathVariable String id) throws IOException {
        Document doc = documentService.getDocumentById(id);

        if (doc == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        File file = new File(doc.getName());
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        Path path = Paths.get(file.getAbsolutePath());
        ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));

        return ResponseEntity.ok().headers(headers).contentLength(file.length())
                .contentType(MediaType.parseMediaType("application/octet-stream")).body(resource);
    }

    @DeleteMapping(path = "/storage/documents/{id}")
    public ResponseEntity<Resource> delete(@PathVariable String id) throws IOException { // deleting soft copy so file is still there only deleting from database
        Document doc = documentService.getDocumentById(id);

        if (doc == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        documentService.delete(doc);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // save file
    private void saveUploadedFiles(List<MultipartFile> files) throws IOException {

        for (MultipartFile file : files) {

            if (file.isEmpty()) {
                continue; // next pls
            }

            byte[] bytes = file.getBytes();
            Path path = Paths.get(UPLOADED_FOLDER + file.getOriginalFilename());
            Files.write(path, bytes);

        }

    }
}

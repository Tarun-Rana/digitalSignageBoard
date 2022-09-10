package com.demo.fileUpload.controller;

import com.demo.fileUpload.model.LoadFile;
import com.demo.fileUpload.service.FileService;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("file")
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file")MultipartFile file) throws IOException {
        System.out.println("Post Started");
        return new ResponseEntity<>(fileService.addFile(file), HttpStatus.OK);
    }

    @PostMapping("/uploadFile")
    public String uploadFile(@RequestParam("file")MultipartFile file) throws IOException {
        System.out.println("Post Started File");
        return fileService.addFile(file);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<ByteArrayResource> download(@PathVariable String id) throws IOException {
        LoadFile loadFile = fileService.downloadFile(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(loadFile.getFileType() ))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + loadFile.getFilename() + "\"")
                .body(new ByteArrayResource(loadFile.getFile()));
    }

    @GetMapping("allInfo")
    public List<GridFSFile> getAllFileDetails(){
        return fileService.getAllFiles();
    }

    @GetMapping("/getFile/{id}")
    public LoadFile getFile(@PathVariable String id){
        try {
            return fileService.downloadFile(id);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
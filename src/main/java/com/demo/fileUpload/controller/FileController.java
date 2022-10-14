package com.demo.fileUpload.controller;

import com.demo.fileUpload.model.FileStore;
import com.demo.fileUpload.model.LoadFile;
import com.demo.fileUpload.model.StringResponse;
import com.demo.fileUpload.repository.FileStoreRepository;
import com.demo.fileUpload.service.FileService;
import org.bson.json.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("file")
public class FileController {

    @Autowired
    private FileService fileService;
    private FileStoreRepository fileStoreRepository;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file")MultipartFile file) throws IOException {
        System.out.println("Post Started");
        return new ResponseEntity<>(fileService.addFile(file), HttpStatus.OK);
    }

    @PostMapping("/uploadFile")
    public Object uploadFile(@RequestParam("file")MultipartFile file) throws IOException {
        System.out.println("Post Started File");
        StringResponse stringResponse= new StringResponse();
        stringResponse.setResponse(fileService.addFile(file).toString());
        return stringResponse;
    }
    @PostMapping("/uploadNewFile")
    public Object uploadNewFile(@RequestParam("file")MultipartFile file) throws IOException {
        System.out.println("Post Started File");

        StringResponse stringResponse= new StringResponse();
        FileStore fileStore = new FileStore();

       // fileStore.setImg(file.getInputStream());
       // stringResponse.setResponse(fileStoreRepository.save());
        return stringResponse;
    }


    @GetMapping("/download/{id}")
    public ResponseEntity<ByteArrayResource> download(@PathVariable String id) throws IOException {
        LoadFile loadFile = fileService.downloadFile(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(loadFile.getFileType() ))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + loadFile.getFilename() + "\"")
                .body(new ByteArrayResource(loadFile.getFile()));
    }

    @GetMapping("/viewLink/{id}")
    public ResponseEntity<ByteArrayResource> viewLink(@PathVariable String id) throws IOException {
        LoadFile loadFile = fileService.downloadFile(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(loadFile.getFileType() ))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + loadFile.getFilename() + "\"")
                .body(new ByteArrayResource(loadFile.getFile()));
    }

    @GetMapping("allInfo")
    public List<LoadFile> getAllFileDetails() throws IOException {
        return fileService.getAllFiles();
    }

    @GetMapping("/getFile/{id}")
    public MultipartFile getFile(@PathVariable String id){
        try {
            LoadFile loadFile = fileService.downloadFile(id);
            return (MultipartFile) loadFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @DeleteMapping("deleteFile/{id}")
    public void deleteFile(@PathVariable String id){
         fileStoreRepository.deleteById(id);
    }



}

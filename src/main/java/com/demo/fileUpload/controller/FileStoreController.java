package com.demo.fileUpload.controller;

import com.demo.fileUpload.model.FileStore;
import com.demo.fileUpload.model.Image;
import com.demo.fileUpload.repository.FileStoreRepository;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/files")
public class FileStoreController {

    @Autowired
    FileStoreRepository fileStoreRepository;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> createAppreciation(
            @RequestPart(value = "files", required = true) MultipartFile[] files) {

        FileStore fileStore = new FileStore();
        try {

            fileStore.setCreateTimestamp(LocalDateTime.now());

            Image image = new Image();
            image.setId(fileStore.getId());
            List<Binary> binaryList = new ArrayList<>();
            List<String> encodedImageList = new ArrayList<String>();

            Arrays.asList(files).stream().forEach(file -> {

                try {
                    binaryList.add(new Binary(BsonBinarySubType.BINARY, file.getBytes()));
                    encodedImageList.add(Base64.getUrlEncoder().encodeToString(file.getBytes()));

                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            image.setImagefiles(binaryList);
            image.setEncodedImageString(encodedImageList);
            fileStore.setImage(image);
            FileStore fileStored = fileStoreRepository.save(fileStore);
            Image createdImage = fileStored.getImage();
            createdImage.setImagefiles(new ArrayList<Binary>());
            fileStored.setImage(createdImage);

            return new ResponseEntity<>(fileStored, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.EXPECTATION_FAILED);
        }

    }
    @GetMapping("/getAllUploads")
    public ResponseEntity<List<FileStore>> getAllAppreciation() {
        try {
            List<FileStore> files = new ArrayList<FileStore>();
            System.out.println("Retrieving all FileStore.....");

            fileStoreRepository.findAll().forEach(files::add);

            if (files.isEmpty()) {
                System.out.println("***** NO files .....");
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            files.stream().forEach(file -> {
                Image image = file.getImage();
                image.setImagefiles(new ArrayList<Binary>());
                file.setImage(image);
            });

            System.out.println("All Appreciation Retrieved .....");
            return new ResponseEntity<>(files, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Error : " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

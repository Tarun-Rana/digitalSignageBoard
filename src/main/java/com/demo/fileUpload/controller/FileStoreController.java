package com.demo.fileUpload.controller;

import com.demo.fileUpload.model.FileStore;
import com.demo.fileUpload.model.Image;
import com.demo.fileUpload.repository.FileStoreRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/files")
public class FileStoreController {

    @Autowired
    FileStoreRepository fileStoreRepository;

    @RequestMapping(value="savefile",method=RequestMethod.POST)
    public String saveimage( @RequestParam MultipartFile file,
                             HttpSession session) throws Exception{
        FileStore fileStore = new FileStore();
        ServletContext context = session.getServletContext();
        String path = ("/Users/tarun/Documents/GitHub/digitalSignageBoard/src/main/resources/images");
        String filename = file.getOriginalFilename().replaceAll(" ","_");
        fileStore.setFileSize(file.getSize()+"");
        fileStore.setFilename(filename);
        fileStore.setFileType(file.getContentType());
        fileStore.setCreateTimestamp(LocalDateTime.now());
        fileStore.setFilePath("images/"+filename);
        fileStoreRepository.save(fileStore);
        System.out.println(path+"/"+filename);

        byte[] bytes = file.getBytes();
        BufferedOutputStream stream =new BufferedOutputStream(new FileOutputStream(
                new File(path + File.separator + filename)));
        stream.write(bytes);
        stream.flush();
        stream.close();

        return fileStore.getId();
    }

    @RequestMapping(value = "/downloadById/{id}", method = RequestMethod.GET,
            produces = MediaType.IMAGE_JPEG_VALUE)

    public void getImagebyId(@PathVariable String id,HttpServletResponse response) throws IOException {

        //var imgFile = new ClassPathResource("images/photo-1543373014-cfe4f4bc1cdf.jpeg");
        FileStore fileStore = new FileStore();
        fileStore=fileStoreRepository.findById(id).orElse(null);
        var imgFile = new ClassPathResource(fileStore.getFilePath());
        response.setContentType(MediaType.IMAGE_JPEG_VALUE);
        StreamUtils.copy(imgFile.getInputStream(), response.getOutputStream());
    }

    @RequestMapping(value = "/sid", method = RequestMethod.GET,
            produces = MediaType.IMAGE_JPEG_VALUE)

    public void getImage(HttpServletResponse response) throws IOException {

        var imgFile = new ClassPathResource("images/photo-1543373014-cfe4f4bc1cdf.jpeg");

        response.setContentType(MediaType.IMAGE_JPEG_VALUE);
        StreamUtils.copy(imgFile.getInputStream(), response.getOutputStream());
    }


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
                    String string =Base64.getEncoder().encodeToString(file.getBytes()).replaceAll("\"", "");
                    encodedImageList.add(string);
                    log.info(encodedImageList.get(0));
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

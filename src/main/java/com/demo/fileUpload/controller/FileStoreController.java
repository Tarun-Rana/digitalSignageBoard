package com.demo.fileUpload.controller;

import com.demo.fileUpload.model.FileStore;
import com.demo.fileUpload.model.Image;
import com.demo.fileUpload.model.StringResponse;
import com.demo.fileUpload.repository.FileStoreRepository;
import com.demo.fileUpload.service.VideoStreamService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
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
    VideoStreamService videoStreamService;

    private String resourcePath="/Users/tarun/Documents/GitHub/digitalSignageBoard/src/main/resources";

    @RequestMapping(value="savefile",method=RequestMethod.POST)
    public StringResponse saveimage(@RequestParam MultipartFile file) throws IOException {
        FileStore fileStore = new FileStore();
        String filename = file.getOriginalFilename().replaceAll(" ","_");
        fileStore.setFileSize(file.getSize()+"");
        fileStore.setFilename(filename);
        fileStore.setFileType(file.getContentType());
        fileStore.setCreateTimestamp(LocalDateTime.now());
        if(fileStore.getFileType().contains("pdf")){
            return savePdf(file,fileStore,filename);
        }else if(fileStore.getFileType().contains("mp4")){
            return saveVideo(file,fileStore,filename); }
       return saveImages(file,fileStore,filename);
    }

    private StringResponse saveVideo(MultipartFile file, FileStore fileStore, String filename) throws IOException {
        String path = resourcePath+"/videos";
        fileStore.setFilePath("videos/"+filename);
        fileStoreRepository.save(fileStore);
        System.out.println(path+"/"+filename);
        byte[] bytes = file.getBytes();
        BufferedOutputStream stream =new BufferedOutputStream(new FileOutputStream(
                path + File.separator + filename));
        stream.write(bytes);
        stream.flush();
        stream.close();

        StringResponse stringResponse = new StringResponse();
        stringResponse.setResponse(fileStore.getId());
        return stringResponse;
    }

    public StringResponse savePdf(MultipartFile file,FileStore fileStore,String filename) throws IOException {
        String path = resourcePath+"/pdf";
        fileStore.setCreateTimestamp(LocalDateTime.now());
        fileStore.setFilePath("pdf/"+filename);
        System.out.println(path+"/"+filename);
        byte[] bytes = file.getBytes();
        BufferedOutputStream stream =new BufferedOutputStream(new FileOutputStream(
                path + File.separator + filename));
        stream.write(bytes);
        stream.flush();
        stream.close();
        PDDocument document = PDDocument.load(new File(resourcePath+"/"+fileStore.getFilePath()));
        fileStore.setCount(document.getNumberOfPages());
        fileStoreRepository.save(fileStore);
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        for (int page = 0; page < document.getNumberOfPages(); ++page) {
            BufferedImage bim = pdfRenderer.renderImageWithDPI(
                    page, 300, ImageType.RGB);
            ImageIOUtil.writeImage(
                    bim, String.format(resourcePath+"/pdf" +
                            "/"+fileStore.getFilename()+"-%d.%s", page + 1, "png"), 300);
        }
        document.close();

        StringResponse stringResponse = new StringResponse();
        stringResponse.setResponse(fileStore.getId()+" "+fileStore.getCount());
        return stringResponse;
    }
    public StringResponse saveImages(MultipartFile file,FileStore fileStore,String filename) throws IOException {

        String path = resourcePath+"/images";
        fileStore.setFilePath("images/"+filename);
        fileStoreRepository.save(fileStore);
        System.out.println(path+"/"+filename);
        byte[] bytes = file.getBytes();
        BufferedOutputStream stream =new BufferedOutputStream(new FileOutputStream(
                new File(path + File.separator + filename)));
        stream.write(bytes);
        stream.flush();
        stream.close();

        StringResponse stringResponse = new StringResponse();
        stringResponse.setResponse(fileStore.getId());
        return stringResponse;
    }

    @RequestMapping(value = "/downloadByPDfId/{id}", method = RequestMethod.GET,
            produces = MediaType.IMAGE_JPEG_VALUE)
    public void downloadByPDfId(@PathVariable String id, HttpServletResponse response) throws IOException {

        //var imgFile = new ClassPathResource("images/photo-1543373014-cfe4f4bc1cdf.jpeg");
        String[] data = id.split(" ");
        FileStore fileStore = fileStoreRepository.findById(data[0]).orElse(null);
        var imgFile = new ClassPathResource(fileStore.getFilePath()+"-"+data[1]+".png");
        response.setContentType(MediaType.IMAGE_JPEG_VALUE);
        StreamUtils.copy(imgFile.getInputStream(), response.getOutputStream());
    }

    @RequestMapping(value = "/downloadById/{id}", method = RequestMethod.GET,
            produces = MediaType.IMAGE_JPEG_VALUE)
    public void getImagebyId(@PathVariable String id,HttpServletResponse response) throws IOException {
        FileStore fileStore = fileStoreRepository.findById(id).orElse(null);
        InputStream is = new FileInputStream(resourcePath+"/"+fileStore.getFilePath());
        response.setContentType(MediaType.IMAGE_JPEG_VALUE);
        StreamUtils.copy(is, response.getOutputStream());
    }

    @GetMapping(value = "download/{id}")
    public ResponseEntity<Resource> getByID(@PathVariable("id") String id) throws IOException {
        System.out.println(id);
        String[] data = id.split(" ");
        FileStore fileStore = fileStoreRepository.findById(data[0]).orElse(null);
        InputStream is= null;
        if(fileStore.getFileType().contains("pdf")){
            is  = new FileInputStream(resourcePath+"/pdf/"+fileStore.getFilename()+"-"+data[1]+".png");
            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf(MediaType.IMAGE_JPEG_VALUE))
                    .body(new ByteArrayResource(is.readAllBytes()));
        }else if(fileStore.getFileType().contains("mp4")) {
            is = new FileInputStream(resourcePath + "/" + fileStore.getFilePath());
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).
                    header("Content-Disposition", "attachment; filename="+fileStore.getFilename(),"video/mp4").
                    body(new ByteArrayResource(is.readAllBytes()));
        }
        is = new FileInputStream(resourcePath + "/" + fileStore.getFilePath());
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(MediaType.IMAGE_JPEG_VALUE))
                .body(new ByteArrayResource(is.readAllBytes()));
    }
    @GetMapping(value = "video/{id}", produces = "video/mp4")
    public ResponseEntity<Resource> getVideoByName(@PathVariable("id") String id) throws IOException {
        System.out.println(id);
        FileStore fileStore = fileStoreRepository.findById(id).orElse(null);
        InputStream is = new FileInputStream(resourcePath + "/" + fileStore.getFilePath());
        return ResponseEntity
                .ok(new ByteArrayResource(is.readAllBytes()));
    }

    @RequestMapping(value = "/downloadAll", method = RequestMethod.GET)

    public List<FileStore> getAllImages() throws IOException {
        List<FileStore> fileStores = new ArrayList<>();
        fileStores=fileStoreRepository.findAll();
        return fileStores;
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

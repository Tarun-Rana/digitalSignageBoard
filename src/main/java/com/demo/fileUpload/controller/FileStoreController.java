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
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
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

    @RequestMapping(value="savefile",method=RequestMethod.POST)
    public StringResponse saveimage(@RequestParam MultipartFile file,
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

        StringResponse stringResponse = new StringResponse();
        stringResponse.setResponse(fileStore.getId());
        return stringResponse;
    }

    @RequestMapping(value="savePDF",method=RequestMethod.POST)
    public StringResponse savePDF(@RequestParam MultipartFile file,
                                    HttpSession session) throws Exception{


        FileStore fileStore = new FileStore();
        ServletContext context = session.getServletContext();
        String path = ("/Users/tarun/Documents/GitHub/digitalSignageBoard/src/main/resources/pdf");
        String filename = file.getOriginalFilename().replaceAll(" ","_");
        fileStore.setFileSize(file.getSize()+"");
        fileStore.setFilename(filename);
        fileStore.setFileType(file.getContentType());
        fileStore.setCreateTimestamp(LocalDateTime.now());
        fileStore.setFilePath("pdf/"+filename);

        System.out.println(path+"/"+filename);

        byte[] bytes = file.getBytes();
        BufferedOutputStream stream =new BufferedOutputStream(new FileOutputStream(
                new File(path + File.separator + filename)));
        stream.write(bytes);
        stream.flush();
        stream.close();

        PDDocument document = PDDocument.load(new File("/Users/tarun/Documents/GitHub/digitalSignageBoard" +
                "/src/main/resources/"+fileStore.getFilePath()));
        fileStore.setCount(document.getNumberOfPages());
        fileStoreRepository.save(fileStore);
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        for (int page = 0; page < document.getNumberOfPages(); ++page) {
            BufferedImage bim = pdfRenderer.renderImageWithDPI(
                    page, 300, ImageType.RGB);
            ImageIOUtil.writeImage(
                    bim, String.format("/Users/tarun/Documents/GitHub/digitalSignageBoard/src/main/resources/pdf" +
                            "/"+fileStore.getFilename()+"-%d.%s", page + 1, "png"), 300);
        }
        document.close();

        StringResponse stringResponse = new StringResponse();
        stringResponse.setResponse(fileStore.getId()+" "+fileStore.getCount());
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

        //var imgFile = new ClassPathResource("images/photo-1543373014-cfe4f4bc1cdf.jpeg");
        FileStore fileStore = fileStoreRepository.findById(id).orElse(null);

        var imgFile = new ClassPathResource(fileStore.getFilePath());
        response.setContentType(MediaType.IMAGE_JPEG_VALUE);
        StreamUtils.copy(imgFile.getInputStream(), response.getOutputStream());

    }

    @GetMapping("/stream/{fileType}/{fileName}")
    public Mono<ResponseEntity<byte[]>> streamVideo(ServerHttpResponse serverHttpResponse, @RequestHeader(value = "Range", required = false) String httpRangeList,
                                                    @PathVariable String id) {
        FileStore fileStore = fileStoreRepository.findById(id).orElse(null);
        return Mono.just(videoStreamService.prepareContent(fileStore.getFilename(),
                fileStore.getFileType(), httpRangeList));
    }
    @GetMapping(
            value = "/get-file/{id}",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public @ResponseBody void getFile(@PathVariable String id,HttpServletResponse response) throws IOException {
        FileStore fileStore = fileStoreRepository.findById(id).orElse(null);
        String path = "/Users/tarun/Documents/GitHub/digitalSignageBoard/target/"+fileStore.getFilePath();
        InputStream in = getClass()
                .getResourceAsStream(path);
        response.setContentType(MediaType.IMAGE_JPEG_VALUE);
   //     StreamUtils.copy(inputStream, response.getOutputStream());
        File initialFile = new File("/Users/tarun/Documents/GitHub/digitalSignageBoard/src/main/resources/"+
                fileStore.getFilePath());
        InputStream targetStream = FileUtils.openInputStream(initialFile);
        StreamUtils.copy(targetStream, response.getOutputStream());
    }

    @RequestMapping(value = "/downloadAll", method = RequestMethod.GET)

    public List<FileStore> getAllImages() throws IOException {

        //var imgFile = new ClassPathResource("images/photo-1543373014-cfe4f4bc1cdf.jpeg");
        List<FileStore> fileStores = new ArrayList<>();
        fileStores=fileStoreRepository.findAll();
        return fileStores;
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

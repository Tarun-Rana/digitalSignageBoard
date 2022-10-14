package com.demo.fileUpload.service;

import com.demo.fileUpload.model.LoadFile;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;

import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class FileService {

    @Autowired
    private GridFsTemplate template;

    @Autowired
    private GridFsOperations operations;

    public Object addFile(MultipartFile upload) throws IOException {

        if(upload.getContentType().equals("application/pdf")){
            generateImageFromPDF(upload.getOriginalFilename(),upload.getContentType());
        }
        //define additional metadata
        DBObject metadata = new BasicDBObject();
        metadata.put("fileSize", upload.getSize());
        System.out.println(upload);
        //store in database which returns the objectID
        Object fileID = template.store(upload.getInputStream(), upload.getOriginalFilename(), upload.getContentType(), metadata);
        //return as a string

        return fileID+"";
    }
    private void generateImageFromPDF(String filename, String extension) throws IOException {
        PDDocument document = PDDocument.load(new File(filename));
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        for (int page = 0; page < document.getNumberOfPages(); ++page) {
            BufferedImage bim = pdfRenderer.renderImageWithDPI(
                    page, 300, ImageType.RGB);
            ImageIOUtil.writeImage(
                    bim, String.format("src/output/pdf-%d.%s", page + 1, extension), 300);
        }
        document.close();
    }


    public LoadFile downloadFile(String id) throws IOException {

        //search file
        GridFSFile gridFSFile = template.findOne( new Query(Criteria.where("_id").is(id)) );


        //convert uri to byteArray
        //save data to LoadFile class
        LoadFile loadFile = new LoadFile();

        if (gridFSFile != null && gridFSFile.getMetadata() != null) {
            loadFile.setFilename( gridFSFile.getFilename() );
            loadFile.setFileType( gridFSFile.getMetadata().get("_contentType").toString() );
            loadFile.setFileSize( gridFSFile.getMetadata().get("fileSize").toString() );
            loadFile.setFile( IOUtils.toByteArray(operations.getResource(gridFSFile).getInputStream()) );
        }

        return loadFile;
    }

    public List<LoadFile> getAllFiles() throws IOException {
        List<GridFSFile> fileList = new ArrayList<GridFSFile>();
        List<LoadFile> loadFiles = new ArrayList<>();
        fileList = template.find(new Query()).into(fileList);
        for (GridFSFile file:fileList
             ) {
            LoadFile loadFile = new LoadFile();
            loadFile.setFile(IOUtils.toByteArray(operations.getResource(file).getInputStream()));
            loadFile.setFilename( file.getFilename() );
            loadFile.setFileType( file.getMetadata().get("_contentType").toString() );
            loadFile.setFileSize( file.getMetadata().get("fileSize").toString() );
            loadFiles.add(loadFile);
        }
        return loadFiles;
    }


}

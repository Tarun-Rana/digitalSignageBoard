package com.demo.fileUpload.model;


import java.io.Serializable;
import java.util.List;

import org.bson.types.Binary;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Images")
public class Image implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private List<Binary> imagefiles;

    private List<String> encodedImageString;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Binary> getImagefiles() {
        return imagefiles;
    }

    public void setImagefiles(List<Binary> imagefiles) {
        this.imagefiles = imagefiles;
    }

    public List<String> getEncodedImageString() {
        return encodedImageString;
    }

    public void setEncodedImageString(List<String> encodedImageString) {
        this.encodedImageString = encodedImageString;
    }



}

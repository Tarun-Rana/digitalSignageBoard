package com.demo.fileUpload.repository;

import com.demo.fileUpload.model.FileStore;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FileStoreRepository extends MongoRepository<FileStore,String> {
}

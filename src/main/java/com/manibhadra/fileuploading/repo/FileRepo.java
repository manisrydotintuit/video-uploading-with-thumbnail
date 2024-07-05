package com.manibhadra.fileuploading.repo;

import com.manibhadra.fileuploading.model.FileUpload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepo extends JpaRepository<FileUpload,Integer> {

}


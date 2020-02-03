package com.example.Storage;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

public interface DocumentRepository extends CrudRepository<Document, Integer> {

    public Document findById(String id);
}

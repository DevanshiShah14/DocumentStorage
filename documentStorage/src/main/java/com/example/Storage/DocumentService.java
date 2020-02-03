package com.example.Storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

@Service
public class DocumentService {

    private static int length = 20;
    @Autowired
    DocumentRepository documentRepository;

    public List<Document> getAllPersons() {
        List<Document> docs = new ArrayList<Document>();
        documentRepository.findAll().forEach(doc -> docs.add(doc));
        return docs;
    }

    public Document getDocumentById(String id) {
        return documentRepository.findById(id);
    }

    public Document saveOrUpdate(Document doc) {
        if (doc.getId() == null) {
            String id = generateRandomString();

            while (documentRepository.findById(id) != null) {
                id = generateRandomString();
            }

            doc.setId(id);
        }
        return documentRepository.save(doc);
    }

    @Transactional
    public void delete(Document doc) {
        documentRepository.delete(doc);
    }

    private static String generateRandomString() {
        String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
        String CHAR_UPPER = CHAR_LOWER.toUpperCase();
        String NUMBER = "0123456789";

        String DATA_FOR_RANDOM_STRING = CHAR_LOWER + CHAR_UPPER + NUMBER;
        SecureRandom random = new SecureRandom();

        if (length < 1) throw new IllegalArgumentException();

        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            // 0-62 (exclusive), random returns 0-61
            int rndCharAt = random.nextInt(DATA_FOR_RANDOM_STRING.length());
            char rndChar = DATA_FOR_RANDOM_STRING.charAt(rndCharAt);

            sb.append(rndChar);
        }

        return sb.toString();
    }
}

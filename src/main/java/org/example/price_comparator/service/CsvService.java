package org.example.price_comparator.service;

import org.example.price_comparator.exceptions.CsvFolderNotFoundException;
import org.example.price_comparator.exceptions.CsvProcessingException;

public interface CsvService {

    void processAllCsvFiles() throws CsvFolderNotFoundException, CsvProcessingException;
    void processCsvFile(String fileName) throws CsvProcessingException;
}



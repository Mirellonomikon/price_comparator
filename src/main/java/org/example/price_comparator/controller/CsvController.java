package org.example.price_comparator.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.price_comparator.exceptions.CsvFolderNotFoundException;
import org.example.price_comparator.exceptions.CsvProcessingException;
import org.example.price_comparator.service.CsvService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/csv")
@Slf4j
public class CsvController {

    private final CsvService csvService;

    public CsvController(CsvService csvService) {
        this.csvService = csvService;
    }

    @PostMapping("/import-all")
    public ResponseEntity<String> processAllCsvFiles() throws CsvFolderNotFoundException, CsvProcessingException {
        csvService.processAllCsvFiles();
        return ResponseEntity.ok("All CSV files imported!");
    }

    @PostMapping("/import")
    public ResponseEntity<String> processCsvFile(@RequestParam String fileName) throws CsvProcessingException {
        csvService.processCsvFile(fileName);
        return ResponseEntity.ok("CSV file imported: " + fileName);
    }
}



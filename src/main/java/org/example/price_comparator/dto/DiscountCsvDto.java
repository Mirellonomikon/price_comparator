package org.example.price_comparator.dto;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DiscountCsvDto {
    @CsvBindByName(column = "product_id")
    private String product_id;

    @CsvBindByName(column = "product_name")
    private String product_name;

    @CsvBindByName(column = "brand")
    private String brand;

    @CsvBindByName(column = "package_quantity")
    private Double package_quantity;

    @CsvBindByName(column = "package_unit")
    private String package_unit;

    @CsvBindByName(column = "product_category")
    private String product_category;

    @CsvBindByName(column = "from_date")
    @CsvDate("yyyy-MM-dd")
    private LocalDate from_date;

    @CsvBindByName(column = "to_date")
    @CsvDate("yyyy-MM-dd")
    private LocalDate to_date;

    @CsvBindByName(column = "percentage_of_discount")
    private Double percentage_of_discount;
}
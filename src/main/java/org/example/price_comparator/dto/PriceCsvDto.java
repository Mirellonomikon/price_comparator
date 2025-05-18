package org.example.price_comparator.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;

@Data
public class PriceCsvDto {
    @CsvBindByName(column = "product_id")
    private String product_id;

    @CsvBindByName(column = "product_name")
    private String product_name;

    @CsvBindByName(column = "product_category")
    private String product_category;

    @CsvBindByName(column = "brand")
    private String brand;

    @CsvBindByName(column = "package_quantity")
    private Double package_quantity;

    @CsvBindByName(column = "package_unit")
    private String package_unit;

    @CsvBindByName(column = "price")
    private Double price;

    @CsvBindByName(column = "currency")
    private String currency;
}
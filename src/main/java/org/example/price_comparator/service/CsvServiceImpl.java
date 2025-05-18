package org.example.price_comparator.service;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.extern.slf4j.Slf4j;
import org.example.price_comparator.dto.DiscountCsvDto;
import org.example.price_comparator.dto.PriceCsvDto;
import org.example.price_comparator.exceptions.CsvFolderNotFoundException;
import org.example.price_comparator.exceptions.CsvProcessingException;
import org.example.price_comparator.model.Discount;
import org.example.price_comparator.model.Price;
import org.example.price_comparator.model.Product;
import org.example.price_comparator.model.Store;
import org.example.price_comparator.repository.DiscountRepository;
import org.example.price_comparator.repository.PriceRepository;
import org.example.price_comparator.repository.ProductRepository;
import org.example.price_comparator.repository.StoreRepository;
import org.example.price_comparator.util.CsvFileParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class CsvServiceImpl implements CsvService {

    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final PriceRepository priceRepository;
    private final DiscountRepository discountRepository;

    @Value("${csv.folder.path:csv_files}")
    private String csvFolderPath;

    public CsvServiceImpl(ProductRepository productRepository, StoreRepository storeRepository, PriceRepository priceRepository, DiscountRepository discountRepository) {
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
        this.priceRepository = priceRepository;
        this.discountRepository = discountRepository;
    }

    @Override
    public void processAllCsvFiles() throws CsvFolderNotFoundException, CsvProcessingException {
        try {
            // Uses ResourcePatternResolver to find the CSV files and avoid using File because of how JARs are packaged
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:" + csvFolderPath + "/*.csv");
            if (resources.length == 0) {
                log.warn("No CSV files found in directory: {}", csvFolderPath);
                return;
            }
            for (Resource resource : resources) {
                String fileName = resource.getFilename();
                if (fileName != null) {
                    processCsvFile(fileName);
                }
            }
        } catch (IOException e) {
            throw new CsvFolderNotFoundException("Error accessing CSV folder: " + e.getMessage());
        }
    }

    @Override
    public void processCsvFile(String fileName) throws CsvProcessingException {
        Resource resource = new ClassPathResource(csvFolderPath + "/" + fileName);
        if (!resource.exists()) {
            throw new CsvProcessingException("CSV file not found: " + fileName);
        }
        String storeName = CsvFileParser.extractStoreName(fileName);
        LocalDate fileDate = CsvFileParser.extractDate(fileName);
        boolean isDiscount = CsvFileParser.isDiscountFile(fileName);
        // Get or create store
        Store store = storeRepository.findByName(storeName)
                .orElseGet(() -> {
                    Store newStore = new Store();
                    newStore.setName(storeName);
                    return storeRepository.save(newStore);
                });
        if (isDiscount) {
            processDiscountFile(resource, store);
        } else {
            processPriceFile(resource, store, fileDate);
        }
    }

    private void processPriceFile(Resource resource, Store store, LocalDate fileDate) throws CsvProcessingException {
        try (InputStreamReader reader = new InputStreamReader(resource.getInputStream())) {
            CsvToBean<PriceCsvDto> csvToBean = new CsvToBeanBuilder<PriceCsvDto>(reader)
                    .withType(PriceCsvDto.class)
                    .withSeparator(';')
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            List<PriceCsvDto> priceDtos = csvToBean.parse();

            for (PriceCsvDto dto : priceDtos) {
                // Get or create product
                Product product = getOrCreateProduct(dto.getProduct_id(), dto.getProduct_name(), dto.getProduct_category(),
                        dto.getBrand(), dto.getPackage_quantity(), dto.getPackage_unit());

                // Create price
                Price price = new Price();
                price.setProduct(product);
                price.setStore(store);
                price.setPrice(dto.getPrice());
                price.setCurrency(dto.getCurrency());
                price.setDate(fileDate);

                priceRepository.save(price);
            }

        } catch (IOException e) {
            throw new CsvProcessingException("Error reading price CSV file", e);
        }
    }

    private void processDiscountFile(Resource resource, Store store) throws CsvProcessingException {
        try (InputStreamReader reader = new InputStreamReader(resource.getInputStream())) {
            CsvToBean<DiscountCsvDto> csvToBean = new CsvToBeanBuilder<DiscountCsvDto>(reader)
                    .withType(DiscountCsvDto.class)
                    .withSeparator(';')
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            List<DiscountCsvDto> discountDtos = csvToBean.parse();

            for (DiscountCsvDto dto : discountDtos) {
                // Get or create product
                Product product = getOrCreateProduct(dto.getProduct_id(), dto.getProduct_name(), dto.getProduct_category(),
                        dto.getBrand(), dto.getPackage_quantity(), dto.getPackage_unit());

                // Create discount
                Discount discount = new Discount();
                discount.setProduct(product);
                discount.setStore(store);
                discount.setPercentageOfDiscount(dto.getPercentage_of_discount());
                discount.setStartDate(dto.getFrom_date());
                discount.setEndDate(dto.getTo_date());

                discountRepository.save(discount);
            }

        } catch (IOException e) {
            throw new CsvProcessingException("Error reading discount CSV file", e);
        }
    }

    private Product getOrCreateProduct(String productId, String productName, String productCategory, String brand, Double packageQuantity, String packageUnit) {
        return productRepository.findById(productId).orElseGet(() -> {
                    Product newProduct = new Product();
                    newProduct.setId(productId);
                    newProduct.setProductName(productName);
                    newProduct.setProductCategory(productCategory);
                    newProduct.setBrand(brand);
                    newProduct.setPackageQuantity(packageQuantity);
                    newProduct.setPackageUnit(packageUnit);
                    return productRepository.save(newProduct);
                });
    }

}


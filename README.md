# Price Comparator
A Spring Boot application that allows users to compare product prices across different stores, track discounts, and receive price alerts.
## Project Overview
The Price Comparator is a REST API designed to help users find the best deals on products across multiple stores. The application processes price data from CSV files, tracks discounts, provides product recommendations, and notifies users when prices drop below their target.
## Project Structure
```
price-comparator/
├── src/
│   ├── main/
│   │   ├── java/com/pricecomparator/
│   │   │   ├── controller/     # REST API endpoints
│   │   │   ├── dto/            # Data Transfer Objects
│   │   │   ├── entity/         # JPA entities
│   │   │   ├── exception/      # Custom exceptions
│   │   │   ├── repository/     # Data access layer
│   │   │   ├── service/        # Business logic
│   │   │   └── PriceComparatorApplication.java
│   │   └── resources/
│   │       ├── csv_files/      # CSV data files
│   │       └── application.properties
│   └── test/                   # Unit and integration tests
├── db_init/
│   └── database_schema.sql     # Database initialization script
├── Dockerfile                  # Docker image definition
├── docker-compose.yml          # Docker Compose configuration
├── pom.xml                     # Maven dependencies
└── README.md                   # Project documentation
```
## Building and Running the Application
### Prerequisites
- Java 17+ (JDK)
- Maven
- Docker and Docker Compose (for containerized deployment)
- PostgreSQL (for local development)
### Local Development
1. Clone the repository
   ```
   git clone https://github.com/yourusername/price-comparator.git
   cd price-comparator
   ```
2. Create a PostgreSQL database named `pricecomparator`
   ```
   createdb pricecomparator
   ```
3. (Optional) Initialize the database with the schema
   ```
   psql -d pricecomparator -f db_init/database_schema.sql
   ```
4. Build the application
   ```
   ./mvnw clean install
   ```
5. Run the application
   ```
   ./mvnw spring-boot:run
   ```
6. Access the application at http://localhost:8081
### Docker Deployment
1. Build and start the containers
   ```
   docker compose build
   docker compose up
   ```
2. Access the application at http://localhost:8081
3. Stop the containers
   ```
   docker compose down
   ```
## Assumptions and Simplifications
1. **User Management**: This application does not include user authentication or authorization. User identification is done via email addresses.
2. **CSV Data Format**: The CSV files are expected to follow a specific format:
    - Price files: `storename_yyyy-MM-dd.csv`
    - Discount files: `storename_discounts_yyyy-MM-dd.csv`
3. **Notification System**: Price alerts are marked as triggered in the database, but actual notifications (emails, SMS, etc.) are not implemented.
4. **Currency**: The application assumes all prices for a product are in the same currency.
5. **Unit Standardization**: For "value per unit" calculations, the application converts units to standard forms (kg, liters) for comparison.
## Features and API Endpoints
### 1. CSV Processing
Import product, price, and discount data from CSV files.
```
POST /api/csv/process-all
```
Processes all CSV files in the configured directory.
```
POST /api/csv/process?fileName=lidl_2023-05-20.csv
```
Processes a specific CSV file.
### 2. Best Discounts
View products with the highest current percentage discounts.
```
GET /api/discounts/best?limit=10
```
Returns the top 10 discounts for the current date.
```
GET /api/discounts/best/by-date?date=2023-05-20&limit=10
```
Returns the top 10 discounts for a specific date.
### 3. New Discounts
View discounts that have been newly added.
```
GET /api/discounts/new?limit=10
```
Returns discounts added in the last 24 hours.
```
GET /api/discounts/new/after-date?date=2023-05-20&limit=10
```
Returns discounts added after a specific date.
```
GET /api/discounts/new/within-24h?date=2023-05-20&limit=10
```
Returns discounts added within 24 hours of a specific date.
### 4. Product Substitutes & Recommendations
Find alternative products and compare value per unit.
```
GET /api/recommendations/substitutes?productId=P12345
```
Returns substitute products for a given product ID.
```
GET /api/recommendations/best-value?category=Dairy
```
Returns products in a category sorted by best value per unit.
### 5. Dynamic Price History Graphs
View price trends over time for products.
```
GET /api/price-history/product/P12345?startDate=2023-01-01&endDate=2023-05-20
```
Returns price history for a specific product.
```
GET /api/price-history/category/Dairy?startDate=2023-01-01&endDate=2023-05-20
```
Returns price history for all products in a category.
```
GET /api/price-history/brand/Nestlé?startDate=2023-01-01&endDate=2023-05-20
```
Returns price history for all products of a specific brand.
### 6. Daily Shopping Basket Monitoring
Optimize shopping lists across multiple stores.
```
POST /api/shopping-basket/optimize
```
Optimizes a shopping basket for the current date.
Request body:
```json
{
  "items": [
    {
      "productId": "P12345",
      "quantity": 2
    },
    {
      "productId": "P67890",
      "quantity": 1
    }
  ]
}
```
```
POST /api/shopping-basket/optimize/by-date?date=2023-05-20
```
Optimizes a shopping basket for a specific date.
### 7. Custom Price Alerts
Set target prices and be notified when prices drop.
```
POST /api/price-alerts
```
Creates a new price alert.
Request body:
```json
{
  "productId": "P12345",
  "storeName": "lidl",
  "targetPrice": 3.99,
  "userEmail": "user@example.com"
}
```
```
GET /api/price-alerts/user/user@example.com
```
Returns all price alerts for a user.
```
POST /api/price-alerts/check
```
Checks all price alerts for triggers.
```
POST /api/price-alerts/check/user@example.com
```
Checks price alerts for a specific user.
```
DELETE /api/price-alerts/123
```
Deletes a specific price alert.
## Example Workflows
### Find the Best Deals for Your Shopping List
1. Create a shopping basket:
   ```
   POST /api/shopping-basket/optimize
   ```
   ```json
   {
     "items": [
       {"productId": "P12345", "quantity": 2},
       {"productId": "P67890", "quantity": 1}
     ]
   }
   ```
2. Review the optimized shopping plan that shows which stores to visit and how much you'll save.
### Set Up Price Alerts
1. Create a price alert:
   ```
   POST /api/price-alerts
   ```
   ```json
   {
     "productId": "P12345",
     "targetPrice": 3.99,
     "userEmail": "user@example.com"
   }
   ```
2. Check if any alerts have been triggered:
   ```
   POST /api/price-alerts/check/user@example.com
   ```
3. View all your alerts:
   ```
   GET /api/price-alerts/user/user@example.com
   ```
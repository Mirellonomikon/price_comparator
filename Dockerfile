FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN chmod +x ./mvnw

RUN ./mvnw dependency:go-offline -B

COPY src ./src
COPY database_schema.sql .

RUN ./mvnw package -DskipTests

ENTRYPOINT ["java", "-jar", "/app/target/price_comparator-0.0.1-SNAPSHOT.jar"]
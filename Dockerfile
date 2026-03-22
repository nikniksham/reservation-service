FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN ./mvnw dependency:go-offline

COPY src ./src

RUN ./mvnw clean package -DskipTests

CMD ["java", "-jar", "target/orders_service-0.0.1-SNAPSHOT.jar"]
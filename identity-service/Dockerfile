FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copy only poms first (cache)
COPY pom.xml pom.xml
COPY common-contracts/pom.xml common-contracts/pom.xml
COPY identity-service/pom.xml identity-service/pom.xml
COPY catalog-service/pom.xml catalog-service/pom.xml

RUN mvn -q -pl identity-service -am -DskipTests dependency:go-offline

# Copy full module folders (works even if src/ is missing or differently structured)
COPY common-contracts common-contracts
COPY identity-service identity-service
COPY catalog-service catalog-service

RUN mvn -q -pl identity-service -am -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/identity-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","app.jar"]

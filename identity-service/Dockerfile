FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copy poms first
COPY pom.xml pom.xml
COPY common-contracts/pom.xml common-contracts/pom.xml
COPY identity-service/pom.xml identity-service/pom.xml
COPY catalog-service/pom.xml catalog-service/pom.xml
COPY reader-lending-service/pom.xml reader-lending-service/pom.xml

RUN mvn -q -pl identity-service -am -DskipTests dependency:go-offline

# Copy sources needed for the build
COPY common-contracts common-contracts
COPY identity-service identity-service

RUN mvn -q -pl identity-service -am -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/identity-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","app.jar"]

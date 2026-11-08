FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src src
RUN mvn -B -q -DskipTests package
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN groupadd --system aitodu && useradd --system --gid aitodu aitodu
COPY --from=build --chown=aitodu:aitodu /build/target/platform-*.jar /app/app.jar
USER aitodu
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=65 -XX:+ExitOnOutOfMemoryError"
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]

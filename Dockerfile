#Verwende ein ARM-kompatibles Maven-Image mit JDK 17 zum Bauen der Anwendung
FROM arm64v8/maven:3.8.4-openjdk-17 AS build
WORKDIR /home/app
COPY . /home/app
RUN mvn clean package -DskipTests

#Verwende ein ARM-kompatibles OpenJDK-Image mit JDK 17 für den Laufzeitcontainer
FROM arm64v8/openjdk:17-slim
COPY --from=build /home/app/target/pokidex-backend-0.0.1-SNAPSHOT.jar /usr/local/lib/pokidex-backend.jar
ENTRYPOINT ["java", "-jar", "/usr/local/lib/pokidex-backend.jar"]

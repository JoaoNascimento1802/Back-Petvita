# Usa a imagem oficial do Java 21 (Eclipse Temurin)
FROM eclipse-temurin:21-jdk-alpine

# Define o diretório de trabalho dentro do contêiner
WORKDIR /app

# Copia o arquivo .jar (que o 'mvn package' criou) para dentro do contêiner
# (Ajuste o nome do .jar se o seu for diferente)
COPY target/PetVita-0.0.1-SNAPSHOT.jar app.jar

# Expõe a porta 8080 (que o Spring usa)
EXPOSE 8080

# Comando para rodar o aplicativo
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
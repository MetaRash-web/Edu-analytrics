# Используем официальный образ Java
FROM maven:3.9.6-eclipse-temurin-22 AS build

# Рабочая директория в контейнере
WORKDIR /app

# Копируем JAR файл
COPY target/Edu-analitycs-0.0.1-SNAPSHOT.jar app.jar

# Открываем порт
EXPOSE 8080

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "app.jar"]
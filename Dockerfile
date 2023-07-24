FROM maven:3-eclipse-temurin AS builder
WORKDIR /build
COPY pom.xml /build
COPY src /build/src
RUN mvn clean package -DskipTests

FROM sapmachine:lts
MAINTAINER Ruslan Vinogradov <get.hard.sp1@gmail.com>
WORKDIR /app
COPY --from=builder /build/target/*.jar /app
ENV TZ="Europe/Moscow"
CMD ["java", "-jar", "telegram_bot.jar"]
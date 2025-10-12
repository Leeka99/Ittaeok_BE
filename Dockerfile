FROM openjdk:21-ea-oraclelinux8

# MySQL 클라이언트 설치
USER root
RUN microdnf install -y mysql

# JAR 파일 복사
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# 실행
ENTRYPOINT ["java","-jar","/app.jar"]

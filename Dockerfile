# --- 1. 빌드 스테이지 (Build Stage) ---
# Gradle이 설치된 Java 25 베이스 이미지 사용
FROM gradle:jdk25 AS build

# 컨테이너 내의 작업 디렉토리를 /app으로 설정
WORKDIR /app

# ci.yml에서 build-args로 모듈 이름을 전달받음 (예: product)
ARG MODULE_NAME

# 프로젝트 전체 소스코드를 /app 디렉토리로 복사
COPY .. .

# gradlew 스크립트에 실행 권한 부여
RUN chmod +x ./gradlew

# 해당 모듈의 빌드 실행 (예: ./gradlew :product:build -x test)
RUN ./gradlew :${MODULE_NAME}:build -x test


# --- 2. 런타임 스테이지 (Runtime Stage) ---
# 애플리케이션 실행을 위한 최소한의 Java 25 런타임 환경 사용
FROM eclipse-temurin:25-jdk-alpine

# ci.yml에서 build-args로 모듈 이름을 전달받음
ARG MODULE_NAME

# Build Stage에서 생성된 해당 모듈의 JAR 파일만 복사
COPY --from=build /app/${MODULE_NAME}/build/libs/*.jar app.jar

# 애플리케이션이 리슨할 포트를 명시
EXPOSE 8080

# 애플리케이션 실행 명령어
ENTRYPOINT ["java", "-jar", "/app.jar"]
FROM eclipse-temurin:25-jre
WORKDIR /app

COPY race-log-backend-0.0.1.jar /app/app.jar

ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

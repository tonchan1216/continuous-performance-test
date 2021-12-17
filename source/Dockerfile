# Arguments
ARG APP_PATH="/usr/local/perf-test"

# Build Image
FROM adoptopenjdk/maven-openjdk11:latest AS builder
LABEL  maintainer "cleartone1216"
ARG APP_PATH

COPY . ${APP_PATH}/
RUN mvn package -Dmaven.test.skip=true -f ${APP_PATH}/pom.xml

# Deploy Image
FROM adoptopenjdk/openjdk11:alpine-slim
ARG APP_PATH

RUN apk --update add tzdata && \
    cp /usr/share/zoneinfo/Asia/Tokyo /etc/localtime && \
    apk del tzdata && \
    rm -rf /var/cache/apk/* && \
    mkdir -p /opt/app/
COPY --from=builder ${APP_PATH}/consumer/target/consumer-0.0.1-SNAPSHOT.jar /opt/app/

CMD java -jar -Dspring.profiles.active=production /opt/app/consumer-0.0.1-SNAPSHOT.jar

# Arguments
ARG APP_PATH="/usr/local/scripts"

# Build Image
FROM adoptopenjdk/maven-openjdk11:latest AS builder
LABEL  maintainer "cleartone1216"
ARG APP_PATH

COPY scripts/ ${APP_PATH}
RUN mvn package -Dmaven.test.skip=true -f ${APP_PATH}/gatling/pom.xml

FROM blazemeter/taurus
ARG APP_PATH
RUN apt-get -y install xmlstarlet bc && \
  apt-get -y clean && \
  rm -rf /var/lib/apt/lists/*
RUN curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip" && \
  unzip awscliv2.zip && \
  ./aws/install

# Taurus working directory  = /bzt-configs
ADD ./load-test.sh /bzt-configs/
RUN chmod 755 /bzt-configs/load-test.sh
COPY scripts/ /bzt-configs/scripts/
COPY --from=builder ${APP_PATH}/gatling/target/gatling-1.0.0.jar /bzt-configs/scripts/gatling/
ENTRYPOINT ["bash","./load-test.sh"]
CMD ["1", "scenario1"]
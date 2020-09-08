ARG APP_VERSION=1.0-SNAPSHOT
ARG PROJECT_HOME=/usr/src

FROM openjdk:8 as stage1

# Env variables
ENV SCALA_VERSION 2.13.1
ENV SBT_VERSION   1.3.8

ARG APP_NAME
ARG APP_VERSION
ARG PROJECT_HOME

# Install Scala
## Piping curl directly in tar
RUN \
  curl -fsL https://downloads.typesafe.com/scala/$SCALA_VERSION/scala-$SCALA_VERSION.tgz | tar xfz - -C /root/ && \
  echo >> /root/.bashrc && \
  echo "export PATH=~/scala-$SCALA_VERSION/bin:$PATH" >> /root/.bashrc

# Install sbt
RUN \
  curl -L -o sbt-$SBT_VERSION.deb https://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb && \
  dpkg -i sbt-$SBT_VERSION.deb && \
  rm sbt-$SBT_VERSION.deb && \
  apt-get update && \
  apt-get install sbt && \
  sbt sbtVersion

# Define working directory
WORKDIR /root

COPY ["build.sbt", "/tmp/build/"]
COPY ["./project/plugins.sbt", "./project/build.properties", "./project/Dependencies.scala", "/tmp/build/project/"]
RUN cd /tmp/build && \
 sbt update && \
 sbt compile

# app
RUN mkdir -p ${PROJECT_HOME}/app

COPY . ${PROJECT_HOME}/app
COPY project/plugins.sbt ${PROJECT_HOME}/app/project/plugins.sbt
COPY project/build.properties ${PROJECT_HOME}/app/project/build.properties
COPY project/Dependencies.scala ${PROJECT_HOME}/app/project/Dependencies.scala

WORKDIR ${PROJECT_HOME}/app

# Build
RUN sbt "${APP_NAME} / dist"

# Unzip the package
RUN unzip ${APP_NAME}/target/universal/${APP_NAME}-${APP_VERSION}.zip

# Stage 2
FROM openjdk:8

ARG APP_NAME
ARG APP_VERSION
ARG PROJECT_HOME

RUN echo ${PROJECT_HOME}/app/${APP_NAME}-${APP_VERSION}

COPY --from=stage1 ${PROJECT_HOME}/app/${APP_NAME}-${APP_VERSION} ${PROJECT_HOME}/app

WORKDIR ${PROJECT_HOME}/app

EXPOSE 9000

RUN chmod +x bin/${APP_NAME}

ENV APPNAME=${APP_NAME}

ENTRYPOINT bin/$APPNAME

FROM ubuntu:22.04

ENV KOTLIN_VERSION=1.8.0
ENV GRADLE_VERSION=8.0
ENV GRADLE_HOME=/opt/gradle

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        openjdk-8-jdk \
        wget \
        unzip \
        ca-certificates && \
    rm -rf /var/lib/apt/lists/*

RUN wget https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip && \
    unzip gradle-${GRADLE_VERSION}-bin.zip -d /opt && \
    rm gradle-${GRADLE_VERSION}-bin.zip && \
    ln -s /opt/gradle-${GRADLE_VERSION} $GRADLE_HOME && \
    ln -s $GRADLE_HOME/bin/gradle /usr/local/bin/gradle

RUN wget https://github.com/JetBrains/kotlin/releases/download/v${KOTLIN_VERSION}/kotlin-compiler-${KOTLIN_VERSION}.zip && \
    unzip kotlin-compiler-${KOTLIN_VERSION}.zip -d /opt && \
    rm kotlin-compiler-${KOTLIN_VERSION}.zip && \
    ln -s /opt/kotlinc/bin/kotlinc /usr/local/bin/kotlinc && \
    ln -s /opt/kotlinc/bin/kotlin /usr/local/bin/kotlin

WORKDIR /app

COPY Test.java .
COPY Test.kt .

COPY build.gradle .
COPY settings.gradle .

RUN gradle --version
RUN gradle build

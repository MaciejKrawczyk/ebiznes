FROM ubuntu:22.04

ENV KOTLIN_VERSION=1.8.0

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        openjdk-8-jdk \
        wget \
        unzip && \
    rm -rf /var/lib/apt/lists/*

RUN wget https://github.com/JetBrains/kotlin/releases/download/v${KOTLIN_VERSION}/kotlin-compiler-${KOTLIN_VERSION}.zip && \
    unzip kotlin-compiler-${KOTLIN_VERSION}.zip -d /opt && \
    rm kotlin-compiler-${KOTLIN_VERSION}.zip && \
    ln -s /opt/kotlinc/bin/kotlinc /usr/local/bin/kotlinc && \
    ln -s /opt/kotlinc/bin/kotlin /usr/local/bin/kotlin

WORKDIR /app

COPY Test.java .
COPY Test1.kt .

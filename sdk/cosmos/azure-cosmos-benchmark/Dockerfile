FROM azul/zulu-openjdk:8
WORKDIR /opt/azure-cosmos-benchmark
COPY ./target/azure-cosmos-benchmark-4.0.0-SNAPSHOT-jar-with-dependencies.jar .
ENTRYPOINT ["java","-jar", "azure-cosmos-benchmark-4.0.0-SNAPSHOT-jar-with-dependencies.jar"]

# Demo - Apache Kafka / InfluxDB / Grafana

## Installation

### Kafka Confluent distribution

- Download archive

```
$ wget http://packages.confluent.io/archive/2.0/confluent-2.0.0-2.11.7.tar.gz
$ tar -xzvf confluent-2.0.0-2.11.7.tar.gz
$ cd confluent-2.0.0

```

- Start Kafka cluster

```
$ ./bin/zookeeper-server-start ./etc/kafka/zookeeper.properties
$ ./bin/kafka-server-start ./etc/kafka/server.properties
```

- **Create Topic**

```
$ ./bin/kafka-topics --create --topic metrics-system --zookeeper localhost:2181 --replication-factor 1 --partitions 3
```

### InfluxDB

- Download archive

```
wget https://s3.amazonaws.com/influxdb/influxdb-0.10.0-1_linux_amd64.tar.gz
```

- Startup

```
./usr/bin/influx
```

### Grafana

- Download and Run docker container

```
docker run \
  -d \
  -p 3000:3000 \
  --volumes /var/lib/grafana:/var/lib/grafana
  --name grafana \
  grafana/grafana:2.6.0
```  

### Build / Run Project


- **Build project**
    1. mvn clean package.
- **Run MetricSystemProducer class**
    1. Download Sigar library : https://sourceforge.net/projects/sigar/
    2. Run main class with that following argument :

```
java -cp demo-kafka-1.0-SNAPSHOT.jar com.zenika.kafka.demo.MetricSystemProducer -Djava.library.path=/tmp/hyperic-sigar-1.6.4/sigar-bin/lib
```

- **Run MetricSystemConsumer class**

```
java -cp demo-kafka-1.0-SNAPSHOT.jar com.zenika.kafka.demo.MetricSystemConsumer
```

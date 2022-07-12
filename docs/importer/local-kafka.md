# Running the Importer against a local Kafka instance
The LWorks fork of the importer supports running against a local instance of Kafka. To
test against a local Kafka instance, take the following steps:

1. Start up a local Kafka instance. Since you generally want to feed data from the importer to other
Kafka processors, it usually makes sense to startup Kafka in [the mirror repo](https://github.com/LedgerWorks/mirror).
From that repo, you can navigate to the [containers/kafka](https://github.com/LedgerWorks/mirror/tree/main/containers/kafka)
directory and run `docker compose -f kafka.docker-compose.yml up`.
2. Start up a Postgres database for the importer. To do that, you can use the Docker compose file at the root
of this repository: `docker compose up db`.
3. Start up the importer using the `local-kafka` Spring profile. To do that, you can run this command
from the root of this repository: `mvn spring-boot:run -pl hedera-mirror-importer`
4. Records should now be sent from the importer to the local Kafka topic named `hedera-record-items`.

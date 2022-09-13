# Change Data Capture with Debezium
In this project, we will implement change Data Capture software design with Debezium.

![Architecture](https://github.com/metin-durmaz/change-data-capture/blob/main/cdc-with-debezium.png)

# Installing

### Up Required Services

```
docker-compose up
```

### Create Debezium Connector

```
curl -i -X POST -H "Accept:application/json" -H "Content-Type:application/json" http://localhost:8083/connectors/ -d @$PWD/create-connector.json
```

#### Connector Configuration

```
{
    "name": "my-connector",
    "config": {
        "connector.class": "io.debezium.connector.postgresql.PostgresConnector",
        "tasks.max": "1",
        "database.hostname": "postgres",
        "database.port": "5432",
        "database.user": "postgres",
        "database.password": "123456",
        "database.dbname": "mydb",
        "database.server.name": "myserver",
        "tombstones.on.delete": "false",
        "snapshot.mode": "never",
        "transforms": "Combine",
        "transforms.Combine.type": "io.debezium.transforms.ByLogicalTableRouter",
        "transforms.Combine.topic.regex": "myserver.(.*).(.*)",
        "transforms.Combine.topic.replacement": "myserver.all_schemas.all_tables"
    }
}
```

### Create Required Keyspace and Tables in Cassandra

```
docker exec -it cassandra cqlsh -u cassandra -p cassandra
```

```
CREATE KEYSPACE cdc_log WITH REPLICATION = {'class' : 'SimpleStrategy','replication_factor' : 1};

use cdc_log;

create table ddl_change(id uuid primary key, object_name text, operation text, date text);

create table dml_change(id uuid primary key, table_name text, operation text, date text,before text, after text);
```

### Up Spring Boot Application

```
docker run -d --name spring-boot-cdc -p 8080:8080 --env-file .env --network=cdc-network metin123/spring-boot-cdc
```

### Connect the Postgresql Database and Execute Some Queries

```
CREATE TABLE person (
	id BIGSERIAL PRIMARY KEY NOT NULL,
	first_name VARCHAR(155) NOT NULL,
	last_name VARCHAR(155) NOT NULL
);

ALTER TABLE person REPLICA IDENTITY FULL;

INSERT INTO person (first_name, last_name) VALUES ('Deneme', 'Deneme');

UPDATE person SET first_name='Metin', last_name='Durmaz' WHERE id=1;

DELETE FROM person WHERE first_name='Metin';

COMMENT ON TABLE person IS 'My Comment';

CREATE FUNCTION my_function()
RETURNS void
LANGUAGE plpgsql
AS
$$
BEGIN
   RAISE NOTICE 'My Function';
END;
$$;

CREATE VIEW my_view AS SELECT text 'Hello World' AS hello;

CREATE SCHEMA my_schema;

CREATE PROCEDURE my_procedure()
LANGUAGE SQL
AS $$
	CREATE TABLE my_table(number1 integer);
$$;
```

### To See Kafka Logs

```
docker-compose -f docker-compose.yml exec kafka /kafka/bin/kafka-console-consumer.sh --bootstrap-server kafka:9092 --from-beginning --property print.key=true --topic myserver.all_schemas.all_tables > log.json
```

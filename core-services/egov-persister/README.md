
# Persister
### Egov persister service
Egov-Persister is a service running independently on seperate server. This service reads the kafka topics and put the messages in DB. We write a yml configuration and put the file path in application.properties.

### DB UML Diagram

- NA

### Service Dependencies
- NA

### Swagger API Contract

- NA

## Service Details

**Features supported**
- Insert/Update Incoming Kafka messages to Database.
- Add Modify kafka msg before putting it into database

**Functionality:**
- Persist data asynchronously using kafka providing very low latency
- Data is persisted in batch
- All operations are transactional
- Values in prepared statement placeholder are fetched using JsonPath
- Easy reference to parent object using ‘{x}’ in jsonPath which substitutes the value of the variable x in the JsonPath with value of x for the child object.(explained in detail below in doc)
- Supported data types **ARRAY("ARRAY"), STRING("STRING"), INT("INT"),DOUBLE("DOUBLE"), FLOAT("FLOAT"), DATE("DATE"), LONG("LONG"),BOOLEAN("BOOLEAN"),JSONB("JSONB")**

**Sample json which we are posting to kafka**
- https://github.com/egovernments/egov-services/blob/master/citizen/citizen-persister/kafka-json.json

**Persister configuration**

Persister uses configuration file to persist data. The key variables are described below:
- serviceName: Name of the service to which this configuration belongs.
- description: Description of the service.
- version: the version of the configuration.
- fromTopic: The kafka topic from which data is fetched
- queryMaps: Contains the list of queries to be executed for the given data.
- query: The query to be executed in form of prepared statement:
    - basePath: base of json object from which data is extrated
    - jsonMaps: Contains the list of jsonPaths for the values in placeholders.
    - jsonPath: The jsonPath to fetch the variable value.


```json
serviceMaps:
 serviceName: student-management-service
 mappings:
 - version: 1.0
   description: Persists student details in studentinfo table
   fromTopic: save-student-info
   isTransaction: true
   queryMaps:
       - query: INSERT INTO studentinfo( id, name, age, marks) VALUES (?, ?, ?, ?);
         basePath: Students.*
         jsonMaps:
          - jsonPath: $.Students.*.id

          - jsonPath: $.Students.*.name

          - jsonPath: $.Students.*.age

          - jsonPath: $.Students.*.marks
```                                  

**Bulk Persister:**

To persist large quantity of data bulk setting in persister can be used. It is mainly used when we migrate data from one system to another. 
The bulk persister have the following two settings:

| variable name           | Default value | Description                                     |
|-------------------------|---------------|-------------------------------------------------|
| `persister.bulk.enabled`| false         | Switch to turn on or off the bulk kafka consumer|
| `persister.batch.size`  | 100           | The batch size for bulk update                  |
| `persister.batch.topics`| (empty)       | Comma-separated topics to force through the batch consumer, in addition to any topic whose name contains '-batch' |
    
Any kafka topic containing data which has to be bulk persisted should have '-batch' appended at the end of topic name example: save-pt-assessment-batch. Alternatively, topics can be routed through the batch consumer without renaming them by listing them (comma-separated) in `persister.batch.topics`. Batch topics (both '-batch' named and those in `persister.batch.topics`) are only excluded from the single (record) consumer when `persister.bulk.enabled=true`.

### Reliability: at-least-once, DB-health pause, dead-letter & parking

The persister is an at-least-once, poison-tolerant writer. Key operational behaviour:

1. **At-least-once delivery.** Auto-commit is turned off (`spring.kafka.consumer.enable-auto-commit=false`). Both listeners commit manually — the single (record) consumer per record (`RECORD` ack), the batch consumer per batch (`BATCH` ack). An offset is committed only after the record has been persisted or durably dead-lettered, so a crash mid-processing results in redelivery, never silent loss.
2. **Failure classification by PostgreSQL SQLSTATE.** Database failures are classified into three buckets:
   - **BENIGN** — `unique_violation` (23505) is treated as an idempotent success (the row is already there from a prior at-least-once delivery).
   - **TRANSIENT** — connection/deadlock/serialization failures (e.g. `08*`, `57*`, `40001`, `40P01`, `53300`, `55P03`, connection-acquisition) are retried in place with back-off and are never dead-lettered.
   - **PERMANENT** — bad-data failures are routed to the dead-letter topic.
3. **DB-health pause/resume.** While the datasource is unreachable the single container is paused, and resumed on recovery, so transient outages retry in place instead of hammering a dead DB. The poll interval is `persister.db-health.check-interval-ms` (default `5000` ms).
4. **Per-record poison isolation.** When a bulk (bare JSON array) message fails, it is split into single-record inserts so only the offending record is dead-lettered while its good siblings still commit.
5. **Idempotent writes.** Service persister configs use `ON CONFLICT (uuid) DO NOTHING` on inserts so that redelivery / dead-letter replay is safe.

**Reliability configuration**

| variable name                                              | Default value                        | Description                                                                          |
|------------------------------------------------------------|--------------------------------------|--------------------------------------------------------------------------------------|
| `spring.kafka.consumer.enable-auto-commit`                 | false                                | Manual offset commit; offsets commit only after durable handling (at-least-once)     |
| `persister.db-health.check-interval-ms`                    | 5000                                 | Interval for the DB-health pause/resume monitor                                       |
| `persister.dead-letter.reprocess.enabled`                  | true                                 | Re-consume the dead-letter topic on the single listener and retry failed records     |
| `persister.dead-letter.reprocess.error-topic`             | egov-persister-deadletter-processed  | Terminal parking topic for records that exhaust retries                               |
| `persister.dead-letter.max-retries`                        | 5                                    | Bounded DLQ retries before a record is parked                                         |
| `persister.custom.executor.enabled`                        | false                                | Optional listener task executor for the single container (off by default)            |
| `persister.custom.executor.max-pool-size`                  | 10                                   | Max pool size for the optional listener task executor                                 |
| `persister.batch.parallel-topic-processing.thread-pool-size` | 1                                 | Thread pool for parallel per-topic processing inside a batch                          |
| `spring.kafka.producer.acks`                               | all                                  | Durable, no-loss dead-letter / parking publishes                                     |
| `spring.kafka.producer.properties.enable.idempotence`      | true                                 | Idempotent producer for dead-letter / parking publishes                              |

The following consumer tuning keys are read live and are not written in `application.properties`: `persister.kafka.partition.assignment.strategy` (default `CooperativeStickyAssignor,RangeAssignor`), `persister.kafka.group.instance.id` (Kafka static membership), and `persister.kafka.session.timeout.ms`.

### Persister Config Versioning

 - Each persister config has a version attribute which signifies the service version, this version can contain custom DSL; defined here, https://github.com/zafarkhaja/jsemver#external-dsl
 - Every incoming request [via kafka] is expected to have a version attribute set, [jsonpath, $.RequestInfo.ver] if versioning is to be applied.
 - If the request version is absent or invalid [not semver] in the incoming request, then a default version defined by the following property in application.properties`default.version=1.0.0` is used.
 - The request version is then matched against the loaded persister configs and applied appropriately.

    
### Kafka Consumers

- From the Kafka topic which are mentioned in the persister config, persister service get message/data and push the data into the particular tables of the database.
- When `persister.dead-letter.reprocess.enabled=true`, the single listener additionally re-consumes the dead-letter topic (`tracer.errorsTopic`, default `egov-persister-deadletter`) to retry previously failed records.

### Kafka Producers

- The persister produces to a **dead-letter topic** (`tracer.errorsTopic`, default `egov-persister-deadletter`) when a record cannot be persisted (permanent/bad-data failure, or a split poison record from a bulk message).
- When `persister.dead-letter.reprocess.enabled=true`, the single listener re-consumes the dead-letter topic and retries each record up to `persister.dead-letter.max-retries` (default `5`). Records that exhaust their retries are produced to a **terminal parking topic** (`persister.dead-letter.reprocess.error-topic`, default `egov-persister-deadletter-processed`).
- Dead-letter and parking publishes are durable (`spring.kafka.producer.acks=all`, `spring.kafka.producer.properties.enable.idempotence=true`).

### Dead-letter & parking topics

| topic                                  | property                                        | Purpose                                                         |
|----------------------------------------|-------------------------------------------------|----------------------------------------------------------------|
| `egov-persister-deadletter`            | `tracer.errorsTopic`                            | Dead-letter topic for failed records; re-consumed on retry     |
| `egov-persister-deadletter-processed`  | `persister.dead-letter.reprocess.error-topic`   | Terminal parking topic for records that exhaust their retries  |

> Parking-topic growth is the terminal-poison signal — monitor it (and dead-letter lag) in ops.

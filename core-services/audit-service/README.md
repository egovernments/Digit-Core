# Audit Service
Audit Service provides a one-stop framework for signing data i.e. creating an immutable data entry to track activities of an entity. Whenever an entity is created/updated/deleted the operation is captured in the data logs and is digitally signed to protect it from tampering.

### Service Dependencies
- egov-persister


### API Details

`BasePath` /audit-service/log/v1/[API endpoint]

##### Method
a) `_create`

This method is use to create an AuditLog.
    
b) `_search`

This method is used to search audit logs. Criteria are sent as query parameters (`tenantId` and `objectId` are mandatory; `module`, `userUUID`, `id`, `offset`, `limit` are optional). Results are ordered by `changedate` descending.


### Kafka Consumers

- process-audit-records

### Kafka Producers

- Following are the Producer topic.
    - **audit-create** :- This topic is used to save audit logs.
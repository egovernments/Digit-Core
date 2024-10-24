
# <Default Data Handler>

This service adds default data for new tenant in the Digit suite of services.

### DB UML Diagram




### Service Dependencies



### Swagger API Contract




## Service Details

Default data handler service listens to create-tenant kafka topic and adds default data for the tenantId received.

### API Details

Default data handler service picks the target tenantId from the kafka topic, mdms schema codes from application.properties, localization locale and modules from application.properties, then creates the default data for the target tenantId in both mdms and localization.


### Kafka Consumers

### Kafka Producers

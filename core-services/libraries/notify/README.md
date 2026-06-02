# notify-service

A pluggable notification microservice. Dispatches notifications over SMS, Email, WhatsApp, and Push channels. Providers are external fat jars loaded at runtime — no provider code lives in this repo.

---

## Architecture overview

```
notify-service/
├── notify-spi/      ← thin contract jar (interfaces, records, enums only)
└── notify-app/      ← Spring Boot 4 runnable service
```

Provider jars (e.g. `notify-provider-twilio`) live in **separate repos**. They are placed into `/providers` at deploy time and discovered at startup via `URLClassLoader` + `ServiceLoader`. The service has no knowledge of provider implementations at build time.

---

## Prerequisites

| Tool | Version |
|---|---|
| Java | 25 |
| Maven | 3.9+ |
| Docker | 24+ |
| Docker Compose | v2 |

---

## Local development

**Start PostgreSQL:**
```bash
docker compose up postgres -d
```

**Run the service:**
```bash
mvn spring-boot:run -pl notify-app -Dspring-boot.run.profiles=local
```

The service starts on `http://localhost:8080`. Swagger UI is at `http://localhost:8080/docs`.

---

## Full stack with Docker

```bash
docker compose up --build
```

This builds the app image and starts both PostgreSQL and notify-service. Provider jars placed in `./providers/` are mounted into the container at `/providers`.

---

## How to implement a provider

1. **Create a Maven project** — e.g. `notify-provider-myvendor`

2. **Add notify-spi as a provided dependency:**
   ```xml
   <dependency>
     <groupId>org.digit.notify</groupId>
     <artifactId>notify-spi</artifactId>
     <version>1.0.0-SNAPSHOT</version>
     <scope>provided</scope>
   </dependency>
   ```

3. **Implement `NotificationChannelProvider`:**
   ```java
   public class MyVendorSmsProvider implements NotificationChannelProvider {
       @Override public Channel supportedChannel() { return Channel.SMS; }
       @Override public String providerName() { return "myvendor"; }
       @Override public DispatchResult send(ChannelMessage msg, Recipient recipient) {
           // call your vendor API here
           return DispatchResult.dispatched();
       }
   }
   ```

4. **Register via ServiceLoader** — create `src/main/resources/META-INF/services/org.digit.notify.spi.NotificationChannelProvider` containing your class name.

5. **Build a fat jar** using `maven-shade-plugin`, excluding `notify-spi`:
   ```xml
   <plugin>
     <groupId>org.apache.maven.plugins</groupId>
     <artifactId>maven-shade-plugin</artifactId>
     <configuration>
       <artifactSet>
         <excludes><exclude>org.digit.notify:notify-spi</exclude></excludes>
       </artifactSet>
     </configuration>
   </plugin>
   ```

6. **Build:**
   ```bash
   mvn package
   ```

7. **Deploy:** copy the shaded jar into `./providers/` and restart the service.

8. **Confirm registration:**
   ```bash
   curl http://localhost:8080/providers
   ```

---

## Environment variables

| Variable | Default | Description |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/notify` | PostgreSQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | `notify` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | `notify` | Database password |
| `NOTIFY_PLUGINS_DIR` | `/providers` | Directory containing provider fat jars |

---

## API docs

Swagger UI: `http://localhost:8080/docs`

OpenAPI JSON: `http://localhost:8080/api-docs`

# eGov Encryption Service

Encryption Service is used to secure the data. It provides functionality to encrypt and decrypt data

### DB UML Diagram

- To Do

### Service Dependencies

- egov-mdms-service


### Swagger API Contract

http://editor.swagger.io/?url=https://raw.githubusercontent.com/egovernments/core-services/gopesh67-patch-8/docs/enc-service-contract.yml#!/

## Service Details

Encryption Service offers following features :

- Encrypt - The service will encrypt the data based on given input parameters and data to be encrypted. The encrypted data will be mandatorily of type string.
- Decrypt - The decryption will happen solely based on the input data (any extra parameters are not required). The encrypted data will have identity of the key used at the time of encryption, the same key will be used for decryption.
- Sign - Encryption Service can hash and sign the data which can be used as unique identifier of the data. This can also be used for searching gicen value from a datastore.
- Verify - Based on the input sign and the claim, it can verify if the the given sign is correct for the provided claim.
- Rotate Key - Encryption Service supports changing the key used for encryption. The old key will still remain with the service which will be used to decrypt old data. All the new data will be encrypted by the new key.

#### Configurations

Following are the properties in application.properties file in egov-enc-service which are configurable.

| Property                     |  Default Value    | Remarks                                                                                                                      | 
| -----------------------------| ------------------| -----------------------------------------------------------------------------------------------------------------------------|
| `master-password`            | asd@#$@$!132123   | Master password for encryption/ decryption.                                                                                  |
| `master.salt`                | qweasdzx          | A salt is random data that is used as an additional input to a one-way function that hashes data, a password or passphrase.  |
| `master.initialvector`       | qweasdzxqwea      | An initialization vector is a fixed-size input to a cryptographic primitive.                                                 |
| `size.key.symmetric`         | 256               | Default size of Symmetric key.                                                                                               |          
| `size.key.asymmetric`        | 1024              | Default size of Asymmetric key.                                                                                              |      
| `size.initialvector`         | 12                | Default size of Initial vector.                                                                                              |

### API Details

a) `POST /crypto/v1/_encrypt`

Encrypts the given input value/s OR values of the object.

b) `POST /crypto/v1/_decrypt`

Decrypts the given input value/s OR values of the object.

c) `/crypto/v1/_sign`

Provide signature for a given value.

d) `POST /crypto/v1/_verify`

Check if the signature is correct for the provided value.

e) `POST /crypto/v1/_rotatekey`

Deactivate the keys for the given tenant and generate new keys. It will deactivate both symmetric and asymmetric keys for the provided tenant.

### Kafka Consumers
NA

### Kafka Producers
NA

## ⚠️ Flyway Migration Note

Starting from version 2.10.0, Flyway migration scripts for `egov_enc_service` no longer assume the default `public` schema.

### Migration Fix (Manual Step Required)

If you are upgrading from an earlier version, follow these steps to avoid migration failure:

1. Check for the **flyway migration history table** for `egov_enc_service` using devops environment variable `SCHEMA_TABLE` for the service. ie. `egov_enc_service_schema`
2. Check the migration history table to ensure only one row exists.
   ```sql
   SELECT * FROM public.egov_enc_service_schema;
   ```
3. Delete the migration history row:
   ```sql
   DELETE FROM public.egov_enc_service_schema;
   ```
4. Deploy the build or restart

### Why?
The migration schema was previously hardcoded to `public`. This change removes that assumption, enabling support for schema-based deployment (e.g., via the `SCHEMA_TABLE` environment variable).
- For deployments with earlier versions, while upgrading to **2.10.0** without this fix, you may encounter deployment issues such as Migration mismatch causing `CrashLoopBackOff`.
- Fresh deployments are not affected.

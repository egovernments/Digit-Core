# Codegen

This service generates codegen jar file which will be used to generate
services by using API specs.



## Service Details
This service is a customized form of codegen made available by swagger.
Running `mvn clean package` will create a jar file. Once the codegen jar is created run the following command to generte the service.
`java -jar {codegen_filename.jar} -l -t -u {api_spec.yaml} -a {artifact_id} -b {base_package_name}`.
The service will be created in the target/output folder. To build the service, navigate to the output folder and run `mvn clean package`.



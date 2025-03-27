from digit_client.models.mdms_v2 import SchemaDefinitionBuilder
from digit_client.services.mdms_v2 import MDMSV2Service
from digit_client.request_config import RequestConfig

# Initialize the service
schema_service = MDMSV2Service()

# Create schema definition using builder pattern
schema_definition = SchemaDefinitionBuilder() \
    .with_tenant_id("pb") \
    .with_code("Owner.cardetials") \
    .with_description("ChargeSlabs") \
    .with_definition({
            "$schema": "http://json-schema.org/draft-07/schema#",
            "type": "object",
            "title": "Root Schema",
            "required": [
                "ownerName",
                "RTO",
                "car"
            ],
            "x-unique": [
                "ownerName"
            ],
            "properties": {
                "ownerName": {
                    "type": "string",
                    "title": "The ownerName Schema"
                },
                "contectNumber": {
                    "type": "array",
                    "title": "The contectNumber Schema",
                    "items": {
                        "type": "number"
                    }
                },
                "address": {
                    "type": "string",
                    "title": "The address Schema"
                },
                "state": {
                    "type": "string"
                },
                "RTO": {
                    "type": "integer",
                    "default": 0
                },
                "car": {
                    "type": "object",
                    "default": {},
                    "title": "The car Schema",
                    "required": [
                        "moduleName",
                        "carNumber"
                    ],
                    "properties": {
                        "moduleName": {
                            "type": "string",
                            "default": "",
                            "title": "The moduleName Schema"
                        },
                        "color": {
                            "type": "string",
                            "default": "",
                            "title": "The color Schema"
                        },
                        "carNumber": {
                            "type": "integer",
                            "default": 0,
                            "title": "The carNumber Schema"
                        },
                        "engine": {
                            "type": "object",
                            "default": {},
                            "title": "The engine Schema",
                            "properties": {
                                "Ename": {
                                    "type": "string",
                                    "default": "",
                                    "title": "The Ename Schema"
                                },
                                "capacity": {
                                    "type": "string",
                                    "default": "",
                                    "title": "The capacity Schema",
                                    "examples": [
                                        ""
                                    ]
                                },
                                "power": {
                                    "type": "string",
                                    "default": "",
                                    "title": "The power Schema"
                                },
                                "average": {
                                    "type": "string",
                                    "default": "",
                                    "title": "The average Schema"
                                }
                            }
                        }
                    }
                }
            }
        }) \
    .build()

# Create request info
RequestConfig.initialize(
    api_id="DIGIT-CLIENT",
    version="1.0.0",
    auth_token="130d2471-998a-48b7-8189-1d3ac43b6be4",
    user_info={
        "id": 595,
        "uuid": "1fda5623-448a-4a59-ad17-657986742d67",
        "userName": "UNIFIED_DEV_USERR",
        "name": "Unified dev user",
        "mobileNumber": "8788788851",
        "emailId": "",
        "type": "EMPLOYEE",
        "roles": [
                {
                    "name": "Localisation admin",
                    "code": "LOC_ADMIN",
                    "tenantId": "LMN"
                },
                {
                    "name": "Employee",
                    "code": "EMPLOYEE",
                    "tenantId": "LMN"
                },
                {
                    "name": "MDMS Admin",
                    "code": "MDMS_ADMIN",
                    "tenantId": "LMN"
                },
                {
                    "name": "SUPER USER",
                    "code": "SUPERUSER",
                    "tenantId": "LMN"
                }
            ],
        "active": True,
        "tenantId": "LMN"
    },
    msg_id="1695889012604|en_IN"
)

# Call the create method
response = schema_service.schema_create(schema_definition)
print(response)

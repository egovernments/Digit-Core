from digit_client.models.mdms_v2 import MdmsBuilder
from digit_client.services.mdms_v2 import MDMSV2Service
from digit_client.request_config import RequestConfig

# Initialize the service
schema_service = MDMSV2Service()

# Create schema definition using builder pattern
schema_definition = MdmsBuilder() \
    .with_tenant_id("LMN") \
    .with_schema_code("Owner.cardetials") \
    .with_data({
        "ownerName": "mustakim N kouji",
        "contectNumber": [123,9090],
        "address": "",
        "state": "",
        "RTO": 105,
        "car": {
            "moduleName": "122",
            "color": "blue",
            "carNumber": 123,
            "engine": {
                "Ename": "",
                "capacity": "",
                "power": "",
                "average": "123"
            }
        }
    }) \
    .with_is_active(True) \
    .build()

# Create request info
RequestConfig.initialize(
    api_id="asset-services",
    version="1.0.0",
    auth_token="a0cf23e2-6027-4eed-9d19-121fd2adddec",
    user_info={
      "id": "1",
      "userName": None,
      "name": None,
      "type": None,
      "mobileNumber": None,
      "emailId": None,
      "roles": None,
      "uuid": "7025d1b3-9e39-46ee-abc4-05e6562e3cba"
    },
    msg_id="search with from and to values"
)

# Create MDMS data
response = schema_service.mdms_create(
    schema_code="Owner.cardetials",  # This should match the schema_code used in the MdmsBuilder
    mdms_data=schema_definition
)

print(response)

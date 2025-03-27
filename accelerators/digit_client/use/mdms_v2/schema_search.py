from digit_client.models.mdms_v2 import SchemaDefCriteriaBuilder
from digit_client.services.mdms_v2 import MDMSV2Service
from digit_client.request_config import RequestConfig

# Initialize the service
schema_service = MDMSV2Service()

# Create search criteria using builder pattern
criteria = SchemaDefCriteriaBuilder() \
    .with_tenant_id("LMN") \
    .with_codes(["Owner.Cardetials"]) \
    .build()

# Create request info
RequestConfig.initialize(
    api_id="asset-services",
    version="",
    msg_id="search with from and to values",
    auth_token="130d2471-998a-48b7-8189-1d3ac43b6be4",
    user_info={
        "id": "1"
    }
)

# Call the search method
response = schema_service.schema_search(criteria)
print(response)

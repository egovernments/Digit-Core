from digit_client.models.mdms_v2 import MdmsCriteriaV2Builder
from digit_client.services.mdms_v2 import MDMSV2Service
from digit_client.request_config import RequestConfig

# Initialize the service
schema_service = MDMSV2Service()

# Create search criteria using builder pattern
criteria = MdmsCriteriaV2Builder() \
    .with_tenant_id("LMN") \
    .with_schema_code("PropertyTax.CancerCess1") \
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

# Call the search method
response = schema_service.mdms_search(criteria)
print(response)

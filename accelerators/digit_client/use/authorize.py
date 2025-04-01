from digit_client.services import AuthorizeService
from digit_client.models import AuthorizationRequest, Role, RoleBuilder, AuthorizationRequestBuilder, ActionRequest, ActionRequestBuilder, Action, ActionBuilder
from digit_client.request_config import RequestConfig, RequestInfo


authorize_service = AuthorizeService()
RequestConfig.initialize(
    api_id="asset-services",
    version="1.0.0",
    auth_token="0e9b955f-5e25-4809-b680-97ef37ccf53f",
    msg_id="authorize_action",
    user_info={
        "id": "1",
        "userName": None,
        "name": None,
        "type": None,
        "mobileNumber": None,
        "emailId": None,
        "roles": None,
        "uuid": "7025d1b3-9e39-46ee-abc4-05e6562e3cba"
    }
)

# Create a role first
role = RoleBuilder() \
    .with_name("Asset Creator") \
    .with_code("ASSET_CREATOR") \
    .with_tenant_id("LMN") \
    .build()

# Create the authorization request
authorization_request = AuthorizationRequestBuilder() \
    .with_uri("string") \
    .add_role(role) \
    .add_tenant_id("LMN") \
    .build()

response = authorize_service.authorize_action(authorization_request)
print(response) 

print(authorize_service.get_mdms_action(ActionRequestBuilder().with_tenant_id("LMN").add_role_code("CITIZEN").with_actions([ActionBuilder().with_parent_module("TradeLicense").with_service_code("ASSET_SERVICE").build()]).build()))









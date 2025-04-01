from digit_client.services.master_data_v1 import MDMSService
from digit_client.models.mdms import (
    MasterDetailBuilder,
    ModuleDetailBuilder,
    MdmsCriteriaReqBuilder,
    MdmsCriteriaBuilder
)
from digit_client.request_config import RequestConfig, RequestInfo
from digit_client.models import Role, UserBuilder

def example_mdms_search():
    # Initialize the MDMS service
    mdms_service = MDMSService()
    
    roles = [
        Role(
            name="Employee",
            code="EMPLOYEE",
            tenant_id="LMN"
        ),
        Role(
            name="System user",
            code="SYSTEM",
            tenant_id="LMN"
        ),
        Role(
            name="Super User",
            code="SUPERUSER",
            tenant_id="LMN"
        )
    ]
    auth_token="0e9b955f-5e25-4809-b680-97ef37ccf53f"
    user_info = UserBuilder()\
        .with_id(181)\
        .with_user_name("TestEggMUSTAKIMNK")\
        .with_uuid("4f6cf5fa-bcb2-4a3a-9dff-9740c04e3a92")\
        .with_type("EMPLOYEE")\
        .with_name("mustak")\
        .with_mobile_number("1234567890")\
        .with_email("xyz@egovernments.org")\
        .with_roles(roles)\
        .with_tenant_id("LMN")\
        .build()
    # Initialize RequestConfig with user info
    RequestConfig.initialize(
        api_id="DIGIT-CLIENT",
        version="1.0.0",
        user_info=user_info.to_dict(),
        auth_token=auth_token
    )
    
    # Create master detail
    master_detail = MasterDetailBuilder()\
        .with_name("CancerCess")\
        .build()
    
    # Create module detail
    module_detail = ModuleDetailBuilder()\
        .with_module_name("PropertyTax")\
        .add_master_detail(master_detail)\
        .build()
    
    # Create MDMS criteria request
    mdms_criteria = MdmsCriteriaBuilder()\
        .with_tenant_id("LMN")\
        .with_module_details([module_detail])\
        .build()
    
    # Get request info with specific message ID
    request_info = RequestConfig.get_request_info(
        action="POST",
        msg_id="5bfa85e7-dfa1-47c8-98b2-747bf552be86"
    )
    
    # Make the search request
    result = mdms_service.search(
        mdms_criteria=mdms_criteria,
        request_info=request_info
    )
    
    print("Search Result with custom request info:", result)
    result1=mdms_service.search(mdms_criteria)
    print("Search Result with default request info:", result1)
    return result

def example_mdms_get():
    # Initialize the MDMS service
    mdms_service = MDMSService()
    
    master_detail = MasterDetailBuilder()\
        .with_name("ConstructionSubType")\
        .build()
    
    # Create module detail
    module_detail = ModuleDetailBuilder()\
        .with_module_name("PropertyTax")\
        .add_master_detail(master_detail)\
        .build()
    
    # Create MDMS criteria request
    mdms_criteria = MdmsCriteriaBuilder()\
        .with_tenant_id("LMN")\
        .with_module_details([module_detail])\
        .build()
    
    # Get MDMS data with default parameters
    result = mdms_service.get(
        module_name="PropertyTax",
        master_name="ConstructionSubType",
        tenant_id="POR",
        mdms_criteria=mdms_criteria
    )
    
    print("Get Result:", result)
    return result

if __name__ == "__main__":
    # Example 1: Search MDMS data
    print("\n=== Example 1: Search MDMS Data ===")
    search_result = example_mdms_search()
    
    # Example 2: Get specific MDMS data
    print("\n=== Example 2: Get MDMS Data ===")
    get_result = example_mdms_get()

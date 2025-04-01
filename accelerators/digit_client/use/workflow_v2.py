from digit_client.services.workflow import WorkflowV2Service
from digit_client.models.workflow import (
    ProcessInstanceBuilder,
    ProcessInstanceSearchCriteriaBuilder,
    StateBuilder,
    WorkflowActionBuilder
)
from digit_client.request_config import RequestConfig, RequestInfo
from digit_client.models import Role, UserBuilder

def example_workflow_transition():
    # Initialize the Workflow service
    workflow_service = WorkflowV2Service()
    
    # Create user info
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
        )
    ]
    
    auth_token = "0e9b955f-5e25-4809-b680-97ef37ccf53f"
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
    
    # Create state for transition
    state = StateBuilder()\
        .with_uuid("state-uuid")\
        .with_state("APPROVED")\
        .build()
    
    # Create workflow action
    action = WorkflowActionBuilder()\
        .with_action("APPROVE")\
        .with_uuid("action-uuid")\
        .with_next_state("APPROVED")\
        .with_roles(["EMPLOYEE"])\
        .build()
    
    # Create process instance
    process_instance = ProcessInstanceBuilder()\
        .with_id("process-id")\
        .with_tenant_id("LMN")\
        .with_business_service("PT")\
        .with_business_id("business-id")\
        .with_action("APPROVE")\
        .with_state(state)\
        .with_module_name("PT")\
        .build()
    
    # Get request info
    request_info = RequestConfig.get_request_info(
        action="POST",
        msg_id="5bfa85e7-dfa1-47c8-98b2-747bf552be86"
    )
    
    # Make the transition request
    result = workflow_service.transition_process(
        process_instances=[process_instance],
        request_info=request_info
    )
    
    print("Transition Result:", result)
    return result

def example_workflow_search():
    # Initialize the Workflow service
    workflow_service = WorkflowV2Service()
    
    # Create search criteria
    search_criteria = ProcessInstanceSearchCriteriaBuilder()\
        .with_tenant_id("LMN")\
        .with_business_service("PT")\
        .with_business_ids(["business-id"])\
        .with_status("APPROVED")\
        .build()
    
    # Get request info
    request_info = RequestConfig.get_request_info(
        action="POST",
        msg_id="5bfa85e7-dfa1-47c8-98b2-747bf552be86"
    )
    
    # Make the search request
    result = workflow_service.search_processes(
        search_criteria=search_criteria,
        request_info=request_info
    )
    
    print("Search Result:", result)
    return result

def example_workflow_count():
    # Initialize the Workflow service
    workflow_service = WorkflowV2Service()
    
    # Create search criteria for counting
    search_criteria = ProcessInstanceSearchCriteriaBuilder()\
        .with_tenant_id("LMN")\
        .with_business_service("PT")\
        .build()
    
    # Get count of processes
    result = workflow_service.count_processes(
        search_criteria=search_criteria
    )
    
    print("Count Result:", result)
    return result

def example_workflow_status_count():
    # Initialize the Workflow service
    workflow_service = WorkflowV2Service()
    
    # Create search criteria for status count
    search_criteria = ProcessInstanceSearchCriteriaBuilder()\
        .with_tenant_id("LMN")\
        .with_business_service("PT")\
        .build()
    
    # Get status count
    result = workflow_service.get_status_count(
        search_criteria=search_criteria
    )
    
    print("Status Count Result:", result)
    return result

def example_workflow_sla_count():
    # Initialize the Workflow service
    workflow_service = WorkflowV2Service()
    
    # Create search criteria for SLA count
    search_criteria = ProcessInstanceSearchCriteriaBuilder()\
        .with_tenant_id("LMN")\
        .with_business_service("PT")\
        .build()
    
    # Get nearing SLA count
    result = workflow_service.get_nearing_sla_count(
        search_criteria=search_criteria
    )
    
    print("SLA Count Result:", result)
    return result

def example_search_escalations():
    # Initialize the Workflow service
    workflow_service = WorkflowV2Service()
    
    # Create search criteria for SLA count
    search_criteria = ProcessInstanceSearchCriteriaBuilder()\
        .with_tenant_id("LMN")\
        .with_business_service("PT")\
        .build()
    
    # Get nearing SLA count
    result = workflow_service.search_escalations(
        search_criteria=search_criteria
    )
    
    print("Escalations Result:", result)
    return result

def example_auto_escalate():
    # Initialize the Workflow service
    workflow_service = WorkflowV2Service()
    
    result = workflow_service.auto_escalate(
        business_service="PT"
    )

    print("Auto Escalate Result:", result)
    return result



if __name__ == "__main__":
    # Example 1: Transition workflow state
    print("\n=== Example 1: Transition Workflow State ===")
    transition_result = example_workflow_transition()
    
    # Example 2: Search workflow processes
    print("\n=== Example 2: Search Workflow Processes ===")
    search_result = example_workflow_search()
    
    # Example 3: Count workflow processes
    print("\n=== Example 3: Count Workflow Processes ===")
    count_result = example_workflow_count()
    
    # Example 4: Get status count
    print("\n=== Example 4: Get Status Count ===")
    status_count_result = example_workflow_status_count()
    
    # Example 5: Get nearing SLA count
    print("\n=== Example 5: Get Nearing SLA Count ===")
    sla_count_result = example_workflow_sla_count() 
    
    # Example 6: Search escalations
    print("\n=== Example 6: Search Escalations ===")
    escalations_result = example_search_escalations()
    
    # Example 7: Auto escalate
    print("\n=== Example 7: Auto Escalate ===")
    auto_escalate_result = example_auto_escalate()

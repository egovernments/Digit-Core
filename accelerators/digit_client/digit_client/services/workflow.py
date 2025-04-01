from typing import Dict, List, Optional, Any
from ..api_client import APIClient
from ..request_config import RequestConfig, RequestInfo
from ..models.workflow import WorkflowAction, WorkflowActionBuilder, State, StateBuilder, ProcessInstance, ProcessInstanceBuilder, ProcessInstanceSearchCriteria, ProcessInstanceSearchCriteriaBuilder

class WorkflowV2Service:
    def __init__(self, api_client: Optional[APIClient] = None):
        self.api_client = api_client or APIClient()
        self.base_url = "egov-workflow-v2/egov-wf/process"
        self.url = "egov-workflow-v2/egov-wf"

    def transition_process(self,
                          process_instances: List[ProcessInstance],
                          request_info: Optional[RequestInfo] = None) -> Dict:
        """
        Transition a workflow process to a new state
        
        Args:
            process_instances: List of process instance objects with transition details
            request_info: Authentication and request metadata
            
        Returns:
            Dict: Response containing process instances and response info
        """
        request_info = request_info or RequestConfig.get_request_info()

        # Create ProcessInstanceRequest structure
        payload = {
            "RequestInfo": request_info.to_dict(),
            "ProcessInstances": [instance.to_dict() for instance in process_instances]
        }

        endpoint = f"{self.base_url}/_transition"
        return self.api_client.post(
            endpoint,
            json_data=payload
        )

    def search_processes(self,
                        search_criteria: Optional[ProcessInstanceSearchCriteria] = None,
                        request_info: Optional[RequestInfo] = None) -> Dict:
        """
        Search for workflow processes based on criteria
        
        Args:
            search_criteria: Optional ProcessInstanceSearchCriteria object
            request_info: Authentication and request metadata
            
        Returns:
            Dict: Response containing process instances and total count
        """
        request_info = request_info or RequestConfig.get_request_info()
        
        # Create RequestInfoWrapper structure
        payload = {
            "RequestInfo": request_info.to_dict()
        }
        
        # Convert search criteria to dict if provided
        params = {}
        if search_criteria:
            params = search_criteria.to_dict()

        endpoint = f"{self.base_url}/_search"
        return self.api_client.post(
            endpoint,
            json_data=payload,
            params=params
        )

    def count_processes(self,
                       search_criteria: Optional[ProcessInstanceSearchCriteria] = None,
                       request_info: Optional[RequestInfo] = None) -> Dict:
        """
        Get count of workflow processes
        
        Args:
            count_criteria: Optional counting filters
            request_info: Authentication and request metadata
            
        Returns:
            Dict: Process count information
        """
        request_info = request_info or RequestConfig.get_request_info()
        
        payload = {
            "RequestInfo": request_info.to_dict()
        }
        
        # Add count criteria if provided
        params = {}
        if search_criteria:
            params = search_criteria.to_dict()

        endpoint = f"{self.base_url}/_count"
        return self.api_client.post(
            endpoint,
            json_data=payload,
            params=params
        )

    def get_nearing_sla_count(self,
                             search_criteria: Optional[ProcessInstanceSearchCriteria] = None,
                             request_info: Optional[RequestInfo] = None) -> Dict:
        """
        Get count of processes nearing their SLA deadline
        
        Args:
            criteria: Optional filtering criteria
            request_info: Authentication and request metadata
            
        Returns:
            Dict: Count of processes nearing SLA
        """
        request_info = request_info or RequestConfig.get_request_info()
        
        payload = {
            "RequestInfo": request_info.to_dict()
        }
        
        # Add criteria if provided
        params = {}
        if search_criteria:
            params = search_criteria.to_dict()

        endpoint = f"{self.base_url}/_nearingslacount"
        return self.api_client.post(
            endpoint,
            json_data=payload,
            params=params
        )

    def get_status_count(self,
                        search_criteria: Optional[ProcessInstanceSearchCriteria] = None,
                        request_info: Optional[RequestInfo] = None) -> Dict:
        """
        Get count of processes by status
        
        Args:
            criteria: Optional filtering criteria
            request_info: Authentication and request metadata
            
        Returns:
            Dict: Count of processes grouped by status
        """
        request_info = request_info or RequestConfig.get_request_info()
        
        payload = {
            "RequestInfo": request_info.to_dict()
        }
        
        # Add criteria if provided
        params = {}
        if search_criteria:
            params = search_criteria.to_dict()

        endpoint = f"{self.base_url}/_statuscount"
        return self.api_client.post(
            endpoint,
            json_data=payload,
            params=params
        )

    def auto_escalate(self,
                     business_service: str,
                     request_info: Optional[RequestInfo] = None) -> Dict:
        """
        Automatically escalate workflow processes for a specific business service
        
        Args:
            business_service: Business service identifier (e.g., "WaterManagement")
            request_info: Authentication and request metadata
            
        Returns:
            Dict: Escalation results
        """
        request_info = request_info or RequestConfig.get_request_info()
        
        # Create RequestInfoWrapper structure
        payload = {
            "RequestInfo": request_info.to_dict()
        }

        endpoint = f"{self.url}/auto/{business_service}/_escalate"
        return self.api_client.post(
            endpoint,
            json_data=payload
        )

    def search_escalations(self,
                          search_criteria: Optional[ProcessInstanceSearchCriteria] = None,
                          request_info: Optional[RequestInfo] = None) -> Dict:
        """
        Search for escalated workflow processes based on criteria
        
        Args:
            criteria: Search criteria for escalated processes
            request_info: Authentication and request metadata
            
        Returns:
            Dict: Matching escalated process instances
        """
        request_info = request_info or RequestConfig.get_request_info()
        
        # Create RequestInfoWrapper structure
        payload = {
            "RequestInfo": request_info.to_dict()
        }
        
        # Convert criteria to query parameters
        params = {}
        if search_criteria:
            params = search_criteria.to_dict()

        endpoint = f"{self.url}/escalate/_search"
        return self.api_client.post(
            endpoint,
            json_data=payload,
            params=params
        )
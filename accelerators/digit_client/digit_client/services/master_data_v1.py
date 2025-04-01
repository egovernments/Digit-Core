from typing import Dict, Optional
from ..api_client import APIClient
from ..models.mdms import MdmsCriteriaReq, MdmsCriteria
from ..request_config import RequestConfig, RequestInfo

class MDMSService:
    def __init__(self, api_client: Optional[APIClient] = None):
        self.api_client = api_client or APIClient()
        self.base_url = "egov-mdms-service/v1"

    def search(self, 
              mdms_criteria: MdmsCriteria,
              request_info: Optional[RequestInfo] = None) -> Dict:
        """
        Search for MDMS data based on criteria
        
        Args:
            mdms_criteria_req (MdmsCriteriaReq): MDMS criteria request containing search parameters
            request_info (Optional[RequestInfo]): Request information containing auth details
            
        Returns:
            Dict: Response containing the MDMS data
        """
        # Get request info if not provided
        if request_info is None:
            request_info = RequestConfig.get_request_info()

        # Build complete request body
        payload = {
            "RequestInfo": request_info.to_dict(),
            "MdmsCriteria": mdms_criteria.to_dict()
        }
        
        endpoint = f"{self.base_url}/_search"
        return self.api_client.post(
            endpoint,
            json_data=payload
        )

    def get(self, 
            module_name: str,
            master_name: str,
            mdms_criteria: MdmsCriteria,
            tenant_id: str = "POR",
            filter: Optional[str] = None,
            request_info: Optional[RequestInfo] = None) -> Dict:
        """
        Get specific MDMS data based on module and master name
        
        Args:
            module_name (str): Name of the module
            master_name (str): Name of the master
            mdms_criteria (MdmsCriteria): MDMS criteria for the request
            tenant_id (str): Tenant ID (default: "POR")
            filter (Optional[str]): Filter criteria
            request_info (Optional[RequestInfo]): Request information containing auth details
            
        Returns:
            Dict: Response containing the MDMS data
        """
        # Get request info if not provided
        if request_info is None:
            request_info = RequestConfig.get_request_info()

        # Build query parameters
        params = {
            "moduleName": module_name,
            "masterName": master_name,
            "tenantId": tenant_id
        }
        if filter:
            params["filter"] = filter

        # Build request body
        mdms_criteria = mdms_criteria.to_dict()

        payload = {
            "RequestInfo": request_info.to_dict(),
            "MdmsCriteria": mdms_criteria
        }

        endpoint = f"{self.base_url}/_get"
        return self.api_client.post(
            endpoint,
            json_data=payload,
            params=params
        ) 
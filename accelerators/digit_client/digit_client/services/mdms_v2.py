from typing import Dict, Optional, List

from ..api_client import APIClient
from ..models.mdms_v2 import SchemaDefCriteria, SchemaDefinition, Mdms, MdmsCriteriaV2
from ..request_config import RequestConfig, RequestInfo

class MDMSV2Service:
    def __init__(self, api_client: Optional[APIClient] = None):
        self.api_client = api_client or APIClient()
        self.base_url = "mdms-v2/schema/v1"
        self.mdms_base_url = "mdms-v2/mdms/v1"

    def schema_create(self, 
               schema_definition: SchemaDefinition,
               request_info: Optional[RequestInfo] = None) -> Dict:
        """
        Create a new schema definition
        
        Args:
            schema_definition: Schema definition to create
            request_info (Optional[RequestInfo]): Request information containing auth details
            
        Returns:
            Dict: Response containing the created schema definition
        """
        # Get request info if not provided
        if request_info is None:
            request_info = RequestConfig.get_request_info()

        # Build complete request body
        payload = {
            "RequestInfo": request_info.to_dict(),
            "SchemaDefinition": schema_definition.to_dict()
        }

        endpoint = f"{self.base_url}/_create"
        return self.api_client.post(
            endpoint,
            json_data=payload
        )
    
    def schema_search(self,
               criteria: SchemaDefCriteria,
               request_info: Optional[RequestInfo] = None) -> Dict:
        """
        Search for schema definitions based on criteria
        
        Args:
            criteria (SchemaDefCriteria): Search criteria for schema definitions
            request_info (Optional[RequestInfo]): Request information containing auth details
            
        Returns:
            Dict: Response containing the matching schema definitions
        """
        # Get request info if not provided
        if request_info is None:
            request_info = RequestConfig.get_request_info()

        # Build complete request body
        payload = {
            "RequestInfo": request_info.to_dict(),
            "SchemaDefCriteria": criteria.to_dict()
        }

        endpoint = f"{self.base_url}/_search"
        return self.api_client.post(
            endpoint,
            json_data=payload
        )

    def mdms_create(self, 
                   schema_code: str,
                   mdms_data: Mdms,
                   request_info: Optional[RequestInfo] = None) -> Dict:
        """
        Create new MDMS data for a specific schema
        
        Args:
            schema_code: Schema code from URL path (e.g., "Owner.car.engine1")
            mdms_data: MDMS data payload
            request_info: Authentication and request metadata
            
        Returns:
            Dict: Created MDMS record
        """
        request_info = request_info or RequestConfig.get_request_info()
        
        payload = {
            "RequestInfo": request_info.to_dict(),
            "Mdms": mdms_data.to_dict()
        }

        endpoint = f"{self.mdms_base_url}/_create/{schema_code}"
        return self.api_client.post(endpoint, json_data=payload)

    def mdms_search(self,
                   criteria: MdmsCriteriaV2,
                   request_info: Optional[RequestInfo] = None) -> Dict:
        """
        Search MDMS records
        
        Args:
            criteria: Search criteria filters
            request_info: Authentication and request metadata
            
        Returns:
            Dict: Search results
        """
        request_info = request_info or RequestConfig.get_request_info()
        
        payload = {
            "RequestInfo": request_info.to_dict(),
            "MdmsCriteria": criteria.to_dict()
        }

        endpoint = f"{self.mdms_base_url}/_search"
        return self.api_client.post(endpoint, json_data=payload)

    def mdms_update(self,
                   schema_code: str,
                   mdms_data: Mdms,
                   request_info: Optional[RequestInfo] = None) -> Dict:
        """
        Update existing MDMS data
        
        Args:
            schema_code: Schema code from URL path (e.g., "TradeLicense.Usage")
            mdms_data: Updated MDMS data with ID
            request_info: Authentication and request metadata
            
        Returns:
            Dict: Updated MDMS record
        """
        request_info = request_info or RequestConfig.get_request_info()
        
        payload = {
            "RequestInfo": request_info.to_dict(),
            "Mdms": mdms_data.to_dict()
        }

        endpoint = f"{self.mdms_base_url}/_update/{schema_code}"
        return self.api_client.post(endpoint, json_data=payload)
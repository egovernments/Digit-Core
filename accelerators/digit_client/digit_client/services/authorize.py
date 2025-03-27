from typing import Dict, List, Optional
from ..api_client import APIClient
from ..models.AuthorizationRequest import AuthorizationRequest  # New model
from ..request_config import RequestConfig, RequestInfo

class AuthorizeService:
    def __init__(self, api_client: Optional[APIClient] = None):
        self.api_client = api_client or APIClient()
        self.base_url = "access/v1"

    def authorize_action(self,
                        authorization_request: AuthorizationRequest,
                        request_info: Optional[RequestInfo] = None,
                        headers: Optional[Dict] = None) -> Dict:
        """
        Authorize an action based on roles and tenant permissions
        
        Args:
            authorization_request: Authorization parameters including roles and tenant IDs
            request_info: Authentication and request metadata
            
        Returns:
            Dict: Authorization response with access decision
        """
        request_info = request_info or RequestConfig.get_request_info()

        payload = {
            "RequestInfo": request_info.to_dict(),
            "AuthorizationRequest": authorization_request.to_dict()
        }

        endpoint = f"{self.base_url}/actions/_authorize"
        return self.api_client.post(
            endpoint,
            json_data=payload,
            additional_headers=headers
        )

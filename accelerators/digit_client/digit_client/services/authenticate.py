from typing import Dict, Optional
from ..api_client import APIClient
from ..models.auth import AuthenticationRequest
from ..request_config import RequestConfig, RequestInfo

class AuthenticationService:
    def __init__(self, api_client: Optional[APIClient] = None):
        self.api_client = api_client or APIClient()
        self.base_url = "user"

    def get_auth_token(self, auth_request: AuthenticationRequest) -> Dict:
        """
        Get authentication token using password grant type
        
        Args:
            auth_request (AuthenticationRequest): Authentication request model containing credentials
            
        Returns:
            Dict: Response containing the auth token
        """
        headers = {
            'Authorization': 'Basic ZWdvdi11c2VyLWNsaWVudDo=',
            'Content-Type': 'application/x-www-form-urlencoded'
        }
        
        return self.api_client.post(
            "oauth/token",
            data=auth_request.to_dict(),
            additional_headers=headers
        )

    def update_password_no_login(self, request_info: Optional[RequestInfo] = None) -> Dict:
        """
        Update password without requiring login
        
        Args:
            request_info (Optional[RequestInfo]): Request information containing auth details
            
        Returns:
            Dict: Response from the password update API
        """
        # Get request info if not provided
        if request_info is None:
            request_info = RequestConfig.get_request_info(
                action="POST",
            )
        headers = {
            'Authorization': 'Basic ZWdvdi11c2VyLWNsaWVudDo=',
            'Content-Type': 'application/x-www-form-urlencoded'
        }
        payload = {
            "RequestInfo": request_info.to_dict()
        }
        
        endpoint = f"{self.base_url}/password/nologin/_update"
        return self.api_client.post(
            endpoint,
            json_data=payload,
            additional_headers=headers
        )

    def logout(self, request_info: Optional[RequestInfo] = None) -> Dict:
        """
        Logout the user using their access token
        
        Args:
            request_info (Optional[RequestInfo]): Request information containing auth details
            
        Returns:
            Dict: Response from the logout API
        """
        # Get request info for logout action if not provided
        if request_info is None:
            request_info = RequestConfig.get_request_info()
        
        payload = {
            "RequestInfo": request_info.to_dict()
        }
        
        # Add access token as query parameter
        params = {
            "access_token": temp_auth_token or request_info.auth_token
        }
        
        endpoint = f"{self.base_url}/_logout"
        return self.api_client.post(
            endpoint,
            json_data=payload,
            params=params
        )
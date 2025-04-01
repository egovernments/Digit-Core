from typing import Dict, List, Optional
from ..api_client import APIClient
from ..models.citizen_user import CitizenUser
from ..models.search_models import UserSearchModel
from ..models.user import User
from ..request_config import RequestConfig, RequestInfo

class UserService:
    def __init__(self, api_client: Optional[APIClient] = None):
        self.api_client = api_client or APIClient()
        self.base_url = "user"

    def create_citizen(self, citizen_user: CitizenUser, request_info: Optional[RequestInfo] = None) -> Dict:
        """
        Create a new citizen user in the DIGIT platform.
        
        Args:
            citizen_user (CitizenUser): The citizen user data
            request_info (RequestInfo, optional): Custom RequestInfo to use. If not provided, uses global RequestConfig
            
        Returns:
            Dict: Response from the user creation API
        """
        # Use provided RequestInfo or get from global config
        if request_info is None:
            request_info = RequestConfig.get_request_info()

        payload = {
            "RequestInfo": request_info.to_dict(),
            "user": citizen_user.to_dict()
        }

        endpoint = f"{self.base_url}/citizen/_create"
        return self.api_client.post(endpoint, json_data=payload)

    def get_user_details(self, tenant_id: str, request_info: Optional[RequestInfo] = None) -> Dict:
        """
        Get user details using access token.
        
        Args:
            tenant_id (str): Tenant ID
            request_info (RequestInfo, optional): Custom RequestInfo to use. If not provided, uses global RequestConfig
            
        Returns:
            Dict: User details from the API
        """
        # Use provided RequestInfo or get from global config
        if request_info is None:
            request_info = RequestConfig.get_request_info()
        
        payload = {
            "RequestInfo": request_info.to_dict()
        }
        
        # Add query parameters
        params = {
            "tenantId": tenant_id
        }
        
        # Use temp_auth_token from parameter if provided, otherwise use auth token from RequestInfo
        if temp_auth_token:
            params["access_token"] = temp_auth_token
        else:
            # Get auth token from RequestInfo
            request_info_dict = request_info.to_dict()
            if request_info_dict.get("authToken"):
                params["access_token"] = request_info_dict["authToken"]
            else:
                raise ValueError("No access token provided and no auth token found in RequestInfo")
        
        endpoint = f"{self.base_url}/_details"
        return self.api_client.post(endpoint, json_data=payload, params=params)

    def update_profile(self, user_profile: User, request_info: Optional[RequestInfo] = None) -> Dict:
        """
        Update user profile.

        Args:
            user_profile (User): The updated user profile data
            request_info (RequestInfo, optional): Custom RequestInfo to use. If not provided, uses global RequestConfig

        Returns:
            Dict: Response from the user profile update API
        """
        # Use provided RequestInfo or get from global config
        if request_info is None:
            request_info = RequestConfig.get_request_info()

        payload = {
            "RequestInfo": request_info.to_dict(),
            "user": user_profile.to_dict()
        }

        endpoint = f"{self.base_url}/profile/_update"
        return self.api_client.post(endpoint, json_data=payload)

    def search_users(self, search_criteria: UserSearchModel, request_info: Optional[RequestInfo] = None) -> Dict:
        """
        Search for users based on given criteria
        
        Args:
            search_criteria (UserSearchModel): The search criteria
            request_info (RequestInfo, optional): Custom RequestInfo to use. If not provided, uses global RequestConfig
            
        Returns:
            Dict: Search results from the API
        """
        # Use provided RequestInfo or get from global config
        if request_info is None:
            request_info = RequestConfig.get_request_info()
        
        # Combine RequestInfo with search criteria at root level
        payload = {
            "RequestInfo": request_info.to_dict(),
            **search_criteria.to_dict()  # Spread search criteria at root level
        }
        
        endpoint = f"{self.base_url}/_search"
        return self.api_client.post(endpoint, json_data=payload)

    def create_user_no_validate(self, citizen_user: CitizenUser, request_info: Optional[RequestInfo] = None) -> Dict:
        """
        Create a new user without validation in the DIGIT platform.
        
        Args:
            citizen_user (CitizenUser): The citizen user data
            request_info (RequestInfo, optional): Custom RequestInfo to use. If not provided, uses global RequestConfig
            
        Returns:
            Dict: Response from the user creation API
        """
        # Use provided RequestInfo or get from global config
        if request_info is None:
            request_info = RequestConfig.get_request_info()

        payload = {
            "RequestInfo": request_info.to_dict(),
            "user": citizen_user.to_dict()
        }

        endpoint = f"{self.base_url}/users/_createnovalidate"
        return self.api_client.post(endpoint, json_data=payload)

    def update_user_no_validate(self, user_profile: User, request_info: Optional[RequestInfo] = None) -> Dict:
        """
        Update a user without validation in the DIGIT platform.
        
        Args:
            user_profile (User): The updated user profile data
            request_info (RequestInfo, optional): Custom RequestInfo to use. If not provided, uses global RequestConfig
            
        Returns:
            Dict: Response from the user update API
        """
        # Use provided RequestInfo or get from global config
        if request_info is None:
            request_info = RequestConfig.get_request_info()

        payload = {
            "RequestInfo": request_info.to_dict(),
            "user": user_profile.to_dict()
        }

        endpoint = f"{self.base_url}/users/_updatenovalidate"
        return self.api_client.post(endpoint, json_data=payload)

    
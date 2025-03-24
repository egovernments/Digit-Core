# from typing import Dict, Optional
# from ..api_client import APIClient
# from ..models.citizen_user import CitizenUser
# from ..request_config import RequestConfig

# class UserService:
#     def __init__(self, api_client: Optional[APIClient] = None):
#         self.api_client = api_client or APIClient()
#         self.base_url = "user/citizen"

#     def create_citizen(self, citizen_user: CitizenUser, temp_auth_token: Optional[str] = None) -> Dict:
#         """
#         Create a new citizen user in the DIGIT platform.
        
#         Args:
#             citizen_user (CitizenUser): The citizen user data
#             temp_auth_token (str, optional): Temporary auth token to use for this request
            
#         Returns:
#             Dict: Response from the user creation API
#         """
#         # Get request info for this specific action
#         request_info = RequestConfig.get_request_info(
#             action="POST",
#             msg_id="create-citizen-user",
#             temp_auth_token=temp_auth_token
#         )

#         payload = {
#             "RequestInfo": request_info.to_dict(),
#             "user": citizen_user.to_dict()
#         }

#         endpoint = f"{self.base_url}/_create"
#         return self.api_client.post(endpoint, json_data=payload)

#     def get_user(self, user_id: str, temp_auth_token: Optional[str] = None) -> Dict:
#         """
#         Get user details by user ID
        
#         Args:
#             user_id (str): The ID of the user to retrieve
#             temp_auth_token (str, optional): Temporary auth token to use for this request
            
#         Returns:
#             Dict: User details from the API
#         """
#         request_info = RequestConfig.get_request_info(
#             action="GET",
#             msg_id="get-user-details",
#             temp_auth_token=temp_auth_token
#         )
        
#         params = {"RequestInfo": request_info.to_dict()}
#         return self.api_client.get(f"{self.base_url}/{user_id}", params=params)

#     # Add more user related methods as needed



from typing import Dict, List, Optional
from ..api_client import APIClient
from ..models.citizen_user import CitizenUser
from ..models.search_models import UserSearchModel
from ..models.user_profile import UserProfileUpdate
from ..request_config import RequestConfig

class UserService:
    def __init__(self, api_client: Optional[APIClient] = None):
        self.api_client = api_client or APIClient()
        self.base_url = "user"

    def create_citizen(self, citizen_user: CitizenUser, temp_auth_token: Optional[str] = None) -> Dict:
        """
        Create a new citizen user in the DIGIT platform.
        
        Args:
            citizen_user (CitizenUser): The citizen user data
            temp_auth_token (str, optional): Temporary auth token to use for this request
            
        Returns:
            Dict: Response from the user creation API
        """
        # Get request info for this specific action
        request_info = RequestConfig.get_request_info(
            action="POST",
            msg_id="create-citizen-user",
            temp_auth_token=temp_auth_token
        )

        payload = {
            "RequestInfo": request_info.to_dict(),
            "user": citizen_user.to_dict()
        }

        endpoint = f"{self.base_url}/citizen/_create"
        return self.api_client.post(endpoint, json_data=payload)

    def get_user_details(self, tenant_id: str,temp_auth_token: Optional[str] = None) -> Dict:
        """
        Get user details using access token. If temp_auth_token is not provided, uses the auth token from RequestInfo.
        
        Args:
            tenant_id (str): Tenant ID
            temp_auth_token (str, optional): Access token of the user. If not provided, uses auth token from RequestInfo
            
        Returns:
            Dict: User details from the API
        """
        request_info = RequestConfig.get_request_info(
            action="POST",
            msg_id="get-user-details",
            temp_auth_token=temp_auth_token
        )
        
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

    def update_profile(self, user_profile: UserProfileUpdate, temp_auth_token: Optional[str] = None) -> Dict:
        """
        Update user profile.

        Args:
            user_profile (UserProfileUpdate): The updated user profile data
            temp_auth_token (str, optional): Temporary auth token to use for this request

        Returns:
            Dict: Response from the user profile update API
        """
        request_info = RequestConfig.get_request_info(
            action="POST",
            msg_id="update-user-profile",
            temp_auth_token=temp_auth_token
        )

        payload = {
            "RequestInfo": request_info.to_dict(),
            "user": user_profile.to_dict()
        }

        endpoint = f"{self.base_url}/profile/_update"
        return self.api_client.post(endpoint, json_data=payload)


    def search_users(self, search_criteria: UserSearchModel, temp_auth_token: Optional[str] = None) -> Dict:
        """
        Search for users based on given criteria
        
        Args:
            search_criteria (UserSearchModel): The search criteria
            temp_auth_token (str, optional): Temporary auth token to use for this request
            
        Returns:
            Dict: Search results from the API
        """
        request_info = RequestConfig.get_request_info(
            action="POST",
            msg_id="search-users",
            temp_auth_token=temp_auth_token
        )
        
        # Combine RequestInfo with search criteria at root level
        payload = {
            "RequestInfo": request_info.to_dict(),
            **search_criteria.to_dict()  # Spread search criteria at root level
        }
        
        endpoint = f"{self.base_url}/_search"
        return self.api_client.post(endpoint, json_data=payload)

    
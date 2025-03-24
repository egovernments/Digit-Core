from typing import Dict, Optional

class TenantService:
    def __init__(self, api_client):
        self.api_client = api_client
        self.base_url = "tenant-management/tenant"

    def create_tenant(self, name: str, email: str, auth_token: Optional[str] = None) -> Dict:
        """
        Create a new tenant in the DIGIT platform.
        
        Args:
            name (str): Name of the tenant
            email (str): Email address for the tenant
            auth_token (Optional[str]): Authentication token. If not provided, uses the default from APIClient
            
        Returns:
            Dict: Response from the tenant creation API
        """
        # Use provided auth token or get from api client
        token = auth_token or self.api_client.auth_token
        
        payload = {
            "tenant": {
                "name": name,
                "email": email
            },
            "RequestInfo": {
                "apiId": "Rainmaker",
                "authToken": token,
                "userInfo": None,
                "msgId": "1742362722972|en_IN",
                "plainAccessRequest": {}
            }
        }
        
        headers = {
            'accept': 'application/json, text/plain, */*',
            'content-type': 'application/json;charset=UTF-8',
            'Authorization': f'Bearer {token}'
        }
        
        endpoint = f"{self.base_url}/_create"
        return self.api_client.post(endpoint, json_data=payload, additional_headers=headers) 
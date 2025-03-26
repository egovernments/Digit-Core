import requests
from .config import Config

class APIClient:
    def __init__(self):
        self.base_url = Config.API_ENDPOINT
        self.auth_token = Config.AUTH_TOKEN

    def get(self, endpoint, params=None):
        headers = {'Authorization': f'Bearer {self.auth_token}'}
        response = requests.get(f"{self.base_url}/{endpoint}", headers=headers, params=params)
        return response.json()

    def post(self, endpoint, json_data=None, data=None, additional_headers=None):
        """
        Make a POST request
        
        Args:
            endpoint (str): API endpoint
            json_data (dict, optional): JSON data to send
            data (dict, optional): Form data to send
            additional_headers (dict, optional): Additional headers to include
            
        Returns:
            dict: Response JSON
        """
        headers = {'Authorization': f'Bearer {self.auth_token}'}
        if additional_headers:
            headers.update(additional_headers)
        print(headers)
        print(json_data)
        print(data)
            
        response = requests.post(
            f"{self.base_url}/{endpoint}", 
            headers=headers,
            json=json_data if json_data is not None else None,
            data=data if data is not None else None
        )
        return response.json()

    # Add more HTTP methods as needed
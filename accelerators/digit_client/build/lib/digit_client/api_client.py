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

    def post(self, endpoint, json_data, additional_headers=None):
        headers = {'Authorization': f'Bearer {self.auth_token}'}
        if additional_headers:
            headers.update(additional_headers)
        response = requests.post(f"{self.base_url}/{endpoint}", headers=headers, json=json_data)
        return response.json()

    # Add more HTTP methods as needed
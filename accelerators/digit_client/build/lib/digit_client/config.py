class Config:
    API_ENDPOINT = "https://sandbox.digit.org"  # Sandbox API endpoint
    AUTH_TOKEN = None  # Will be set during runtime

    @classmethod
    def set_auth_token(cls, token: str):
        """Set the authentication token for API requests"""
        cls.AUTH_TOKEN = token

    @classmethod
    def set_api_endpoint(cls, endpoint: str):
        """Set the API endpoint (useful for switching between environments)"""
        cls.API_ENDPOINT = endpoint
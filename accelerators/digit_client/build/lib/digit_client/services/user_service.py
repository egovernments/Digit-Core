from ..api_client import APIClient

class UserService:
    def __init__(self):
        self.api_client = APIClient()

    def create_user(self, user_data):
        return self.api_client.post("user/create", user_data)

    def get_user(self, user_id):
        return self.api_client.get(f"user/{user_id}")

    # Add more user related methods as needed
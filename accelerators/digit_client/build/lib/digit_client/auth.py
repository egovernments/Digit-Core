import requests
import json

class DigitAuth:
    BASE_URL = "https://sandbox.digit.org"
    
    @staticmethod
    def register_user(email, tenant_name):
        url = f"{DigitAuth.BASE_URL}/tenant-management/tenant/_create"
        payload = {
            "tenant": {
                "name": tenant_name,
                "email": email
            },
            "RequestInfo": {
                "apiId": "Rainmaker",
                "authToken": None,
                "userInfo": None,
                "msgId": "registration",
                "plainAccessRequest": {}
            }
        }
        response = requests.post(url, json=payload)
        return response.json()

    @staticmethod
    def send_otp(email, tenant_id):
        url = f"{DigitAuth.BASE_URL}/user-otp/v1/_send"
        params = {"tenantId": tenant_id}
        payload = {
            "otp": {
                "userName": email,
                "type": "login",
                "tenantId": tenant_id,
                "userType": "EMPLOYEE"
            },
            "RequestInfo": {
                "apiId": "Rainmaker",
                "authToken": None,
                "userInfo": None,
                "msgId": "otp_request",
                "plainAccessRequest": {}
            }
        }
        response = requests.post(url, params=params, json=payload)
        return response.json()

    @staticmethod
    def validate_otp(email, otp, tenant_id):
        url = f"{DigitAuth.BASE_URL}/user/oauth/token"
        
        # Form encoded payload
        payload = {
            'username': email,
            'password': otp,
            'tenantId': tenant_id,
            'userType': 'EMPLOYEE',
            'scope': 'read',
            'grant_type': 'password'
        }
        
        headers = {
            'accept': 'application/json, text/plain, */*',
            'accept-language': 'en-US,en;q=0.9',
            'authorization': 'Basic ZWdvdi11c2VyLWNsaWVudDo=',
            'content-type': 'application/x-www-form-urlencoded',
            'origin': 'https://sandbox.digit.org',
            'referer': f'https://sandbox.digit.org/sandbox-ui/{tenant_id}/employee/user/login/otp',
            'user-agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36'
        }
        
        response = requests.post(url, headers=headers, data=payload)
        return response.json() 
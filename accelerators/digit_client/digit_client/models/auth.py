from dataclasses import dataclass
from typing import Optional

@dataclass
class AuthenticationRequest:
    """
    Model for authentication request parameters
    """
    username: str
    password: str
    tenant_id: str
    grant_type: str = 'password'
    scope: str = 'read'
    user_type: str = 'EMPLOYEE'

    def to_dict(self) -> dict:
        return {
            'grant_type': self.grant_type,
            'scope': self.scope,
            'username': self.username,
            'password': self.password,
            'tenantId': self.tenant_id,
            'userType': self.user_type
        }

class AuthenticationRequestBuilder:
    """Builder class for creating AuthenticationRequest objects"""
    
    def __init__(self):
        # Required fields
        self._username: Optional[str] = None
        self._password: Optional[str] = None
        self._tenant_id: Optional[str] = None
        
        # Optional fields with defaults
        self._grant_type: str = 'password'
        self._scope: str = 'read'
        self._user_type: str = 'EMPLOYEE'

    def with_username(self, username: str) -> 'AuthenticationRequestBuilder':
        self._username = username
        return self

    def with_password(self, password: str) -> 'AuthenticationRequestBuilder':
        self._password = password
        return self

    def with_tenant_id(self, tenant_id: str) -> 'AuthenticationRequestBuilder':
        self._tenant_id = tenant_id
        return self

    def with_grant_type(self, grant_type: str) -> 'AuthenticationRequestBuilder':
        self._grant_type = grant_type
        return self

    def with_scope(self, scope: str) -> 'AuthenticationRequestBuilder':
        self._scope = scope
        return self

    def with_user_type(self, user_type: str) -> 'AuthenticationRequestBuilder':
        self._user_type = user_type
        return self

    def build(self) -> AuthenticationRequest:
        """Build and validate the AuthenticationRequest object"""
        # Validate required fields
        required_fields = {
            'username': self._username,
            'password': self._password,
            'tenant_id': self._tenant_id
        }

        missing_fields = [field for field, value in required_fields.items() if value is None]
        if missing_fields:
            raise ValueError(f"Missing required fields: {', '.join(missing_fields)}")

        return AuthenticationRequest(
            username=self._username,
            password=self._password,
            tenant_id=self._tenant_id,
            grant_type=self._grant_type,
            scope=self._scope,
            user_type=self._user_type
        ) 
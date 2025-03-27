from dataclasses import dataclass, field
from typing import Set, Optional, Dict, Any, List

@dataclass(frozen=True)
class Role:
    """
    Model representing a user role
    """
    id: Optional[int] = None
    name: Optional[str] = None
    code: Optional[str] = None
    tenant_id: Optional[str] = None

    def __post_init__(self):
        if self.name and len(self.name) > 32:
            raise ValueError("name must be at most 32 characters")
        if self.code and len(self.code) > 50:
            raise ValueError("code must be at most 50 characters")
        if self.tenant_id and len(self.tenant_id) > 50:
            raise ValueError("tenant_id must be at most 50 characters")

    def to_dict(self) -> Dict[str, Any]:
        return {
            "id": self.id,
            "name": self.name,
            "code": self.code,
            "tenantId": self.tenant_id
        }

@dataclass
class AuthorizationRequest:
    """
    Model representing an authorization request
    """
    roles: Set[Role]
    uri: str
    tenant_ids: Set[str]

    def __post_init__(self):
        if not self.roles:
            raise ValueError("At least one role is required")
        if not self.uri:
            raise ValueError("URI is required")
        if not self.tenant_ids:
            raise ValueError("At least one tenant ID is required")

    def to_dict(self) -> Dict[str, Any]:
        return {
            "roles": [role.to_dict() for role in self.roles],
            "uri": self.uri,
            "tenantIds": list(self.tenant_ids)
        }

class RoleBuilder:
    """Builder for creating Role objects"""
    def __init__(self):
        self._id: Optional[int] = None
        self._name: Optional[str] = None
        self._code: Optional[str] = None
        self._tenant_id: Optional[str] = None

    def with_id(self, role_id: int) -> 'RoleBuilder':
        self._id = role_id
        return self

    def with_name(self, name: str) -> 'RoleBuilder':
        self._name = name
        return self

    def with_code(self, code: str) -> 'RoleBuilder':
        self._code = code
        return self

    def with_tenant_id(self, tenant_id: str) -> 'RoleBuilder':
        self._tenant_id = tenant_id
        return self

    def build(self) -> Role:
        return Role(
            id=self._id,
            name=self._name,
            code=self._code,
            tenant_id=self._tenant_id
        )

class AuthorizationRequestBuilder:
    """Builder for creating AuthorizationRequest objects"""
    def __init__(self):
        self._roles: Set[Role] = set()
        self._uri: Optional[str] = None
        self._tenant_ids: Set[str] = set()

    def with_uri(self, uri: str) -> 'AuthorizationRequestBuilder':
        self._uri = uri
        return self

    def add_role(self, role: Role) -> 'AuthorizationRequestBuilder':
        self._roles.add(role)
        return self

    def add_tenant_id(self, tenant_id: str) -> 'AuthorizationRequestBuilder':
        self._tenant_ids.add(tenant_id)
        return self

    def build(self) -> AuthorizationRequest:
        if not self._uri:
            raise ValueError("URI is required")
        if not self._roles:
            raise ValueError("At least one role is required")
        if not self._tenant_ids:
            raise ValueError("At least one tenant ID is required")
            
        return AuthorizationRequest(
            roles=self._roles,
            uri=self._uri,
            tenant_ids=self._tenant_ids
        )

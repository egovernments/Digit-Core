from typing import List, Optional
from dataclasses import dataclass
from copy import deepcopy
from .AuthorizationRequest import Role

# @dataclass
# class Role:
#     name: str
#     code: str
#     tenant_id: str

#     def to_dict(self) -> dict:
#         return {
#             "name": self.name,
#             "code": self.code,
#             "tenantId": self.tenant_id
#         }

@dataclass
class User:
    id: int
    uuid: str
    user_name: str
    name: str
    mobile_number: str
    email_id: str
    type: str
    roles: List[Role]
    tenant_id: str

    def to_dict(self) -> dict:
        return {
            "id": self.id,
            "uuid": self.uuid,
            "userName": self.user_name,
            "name": self.name,
            "mobileNumber": self.mobile_number,
            "emailId": self.email_id,
            "type": self.type,
            "roles": [role.to_dict() for role in self.roles],
            "tenantId": self.tenant_id  
        }

class UserBuilder:
    """Builder class for creating UserProfileUpdate objects"""
    
    def __init__(self):
        self._roles: List[Role] = []
        self._id: Optional[int] = None
        self._uuid: Optional[str] = None
        self._user_name: Optional[str] = None
        self._name: Optional[str] = None
        self._mobile_number: Optional[str] = None
        self._email_id: Optional[str] = None
        self._type: str = "CITIZEN"
        self._tenant_id: Optional[str] = None

    def with_id(self, id: int) -> 'UserProfileUpdateBuilder':
        self._id = id
        return self

    def with_uuid(self, uuid: str) -> 'UserProfileUpdateBuilder':
        self._uuid = uuid
        return self

    def with_user_name(self, user_name: str) -> 'UserProfileUpdateBuilder':
        self._user_name = user_name
        return self

    def with_name(self, name: str) -> 'UserProfileUpdateBuilder':
        self._name = name
        return self

    def with_mobile_number(self, mobile_number: str) -> 'UserProfileUpdateBuilder':
        self._mobile_number = mobile_number
        return self

    def with_email(self, email_id: str) -> 'UserProfileUpdateBuilder':
        self._email_id = email_id
        return self


    def with_type(self, type: str) -> 'UserProfileUpdateBuilder':
        self._type = type
        return self

    def with_role(self, role: Role) -> 'UserProfileUpdateBuilder':
        self._roles.append(deepcopy(role))
        return self

    def with_roles(self, roles: List[Role]) -> 'UserProfileUpdateBuilder':
        self._roles.extend(deepcopy(roles))
        return self

    def with_tenant_id(self, tenant_id: str) -> 'UserProfileUpdateBuilder':
        self._tenant_id = tenant_id
        return self


    def build(self) -> User:
        """Build and validate the UserProfileUpdate object"""

        return User(
            id=self._id,
            uuid=self._uuid,
            user_name=self._user_name,
            name=self._name,
            mobile_number=self._mobile_number,
            email_id=self._email_id,
            type=self._type,
            roles=self._roles,
            tenant_id=self._tenant_id
        ) 
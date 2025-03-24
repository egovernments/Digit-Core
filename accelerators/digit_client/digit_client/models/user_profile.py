from typing import List, Optional
from dataclasses import dataclass
from copy import deepcopy

@dataclass
class UserRole:
    name: str
    code: str
    tenant_id: str

    def to_dict(self) -> dict:
        return {
            "name": self.name,
            "code": self.code,
            "tenantId": self.tenant_id
        }

@dataclass
class UserProfileUpdate:
    id: int
    uuid: str
    user_name: str
    name: str
    mobile_number: str
    email_id: str
    locale: str
    type: str
    roles: List[UserRole]
    active: bool
    tenant_id: str
    permanent_city: Optional[str] = None

    def to_dict(self) -> dict:
        return {
            "id": self.id,
            "uuid": self.uuid,
            "userName": self.user_name,
            "name": self.name,
            "mobileNumber": self.mobile_number,
            "emailId": self.email_id,
            "locale": self.locale,
            "type": self.type,
            "roles": [role.to_dict() for role in self.roles],
            "active": self.active,
            "tenantId": self.tenant_id,
            "permanentCity": self.permanent_city
        }

class UserProfileUpdateBuilder:
    """Builder class for creating UserProfileUpdate objects"""
    
    def __init__(self):
        self._roles: List[UserRole] = []
        self._id: Optional[int] = None
        self._uuid: Optional[str] = None
        self._user_name: Optional[str] = None
        self._name: Optional[str] = None
        self._mobile_number: Optional[str] = None
        self._email_id: Optional[str] = None
        self._locale: str = "en_IN"
        self._type: str = "CITIZEN"
        self._active: bool = True
        self._tenant_id: Optional[str] = None
        self._permanent_city: Optional[str] = None

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

    def with_locale(self, locale: str) -> 'UserProfileUpdateBuilder':
        self._locale = locale
        return self

    def with_type(self, type: str) -> 'UserProfileUpdateBuilder':
        self._type = type
        return self

    def with_role(self, role: UserRole) -> 'UserProfileUpdateBuilder':
        self._roles.append(deepcopy(role))
        return self

    def with_roles(self, roles: List[UserRole]) -> 'UserProfileUpdateBuilder':
        self._roles.extend(deepcopy(roles))
        return self

    def with_active(self, active: bool) -> 'UserProfileUpdateBuilder':
        self._active = active
        return self

    def with_tenant_id(self, tenant_id: str) -> 'UserProfileUpdateBuilder':
        self._tenant_id = tenant_id
        return self

    def with_permanent_city(self, permanent_city: str) -> 'UserProfileUpdateBuilder':
        self._permanent_city = permanent_city
        return self

    def build(self) -> UserProfileUpdate:
        """Build and validate the UserProfileUpdate object"""
        # Validate required fields
        required_fields = {
            'id': self._id,
            'uuid': self._uuid,
            'user_name': self._user_name,
            'name': self._name,
            'mobile_number': self._mobile_number,
            'email_id': self._email_id,
            'tenant_id': self._tenant_id
        }

        missing_fields = [field for field, value in required_fields.items() if value is None]
        if missing_fields:
            raise ValueError(f"Missing required fields: {', '.join(missing_fields)}")

        if not self._roles:
            raise ValueError("At least one role is required")

        return UserProfileUpdate(
            id=self._id,
            uuid=self._uuid,
            user_name=self._user_name,
            name=self._name,
            mobile_number=self._mobile_number,
            email_id=self._email_id,
            locale=self._locale,
            type=self._type,
            roles=self._roles,
            active=self._active,
            tenant_id=self._tenant_id,
            permanent_city=self._permanent_city
        ) 
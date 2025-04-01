from typing import List, Optional
from dataclasses import dataclass, field

@dataclass
class UserSearchModel:
    """Model for user search criteria"""
    tenantId: str  # Using exact field names from API
    userType: Optional[str] = None  # Changed from type to userType
    active: Optional[bool] = None
    uuid: Optional[List[str]] = None
    userName: Optional[str] = None  # Changed from user_name to userName
    
    def to_dict(self) -> dict:
        """Convert the search model to a dictionary for API request"""
        search_dict = {
            "tenantId": self.tenantId
        }
        
        if self.userType is not None:
            search_dict["userType"] = self.userType
            
        if self.active is not None:
            search_dict["active"] = str(self.active).lower()  # Convert to "true" or "false" string
            
        if self.uuid:
            search_dict["uuid"] = self.uuid
            
        if self.userName:
            search_dict["userName"] = self.userName
            
        return search_dict 
    
class UserSearchModelBuilder:
    def __init__(self):
        self._tenant_id = None
        self._user_type = None
        self._active = None
        self._uuid = None
        self._user_name = None
        
    def with_tenant_id(self, tenant_id: str) -> 'UserSearchModelBuilder':
        self._tenant_id = tenant_id
        return self
    
    def with_user_type(self, user_type: str) -> 'UserSearchModelBuilder':
        self._user_type = user_type
        return self
    
    def with_active(self, active: bool) -> 'UserSearchModelBuilder':
        self._active = active   
        return self
    
    def with_uuid(self, uuid: List[str]) -> 'UserSearchModelBuilder':
        self._uuid = uuid
        return self
        
    def with_user_name(self, user_name: str) -> 'UserSearchModelBuilder':
        self._user_name = user_name
        return self
    
    def build(self) -> UserSearchModel:
        return UserSearchModel( 
            tenantId=self._tenant_id,
            userType=self._user_type,
            active=self._active,
            uuid=self._uuid,
            userName=self._user_name
        )
 
        

from dataclasses import dataclass, field
from typing import List, Optional, Dict, Any
from datetime import datetime
from ..request_config import RequestInfo

@dataclass
class Action:
    """
    Model representing an action
    """
    id: Optional[int] = None
    name: Optional[str] = None
    url: Optional[str] = None
    display_name: Optional[str] = None
    order_number: Optional[int] = None
    query_params: Optional[str] = None
    parent_module: Optional[str] = None
    enabled: bool = False
    service_code: Optional[str] = None
    tenant_id: Optional[str] = None
    created_date: Optional[datetime] = None
    created_by: Optional[int] = None
    last_modified_date: Optional[datetime] = None
    last_modified_by: Optional[int] = None
    path: Optional[str] = None
    navigation_url: Optional[str] = None
    left_icon: Optional[str] = None
    right_icon: Optional[str] = None

    def __post_init__(self):
        if self.name and len(self.name) > 100:
            raise ValueError("name must be at most 100 characters")
        if self.url and len(self.url) > 100:
            raise ValueError("url must be at most 100 characters")
        if self.display_name and len(self.display_name) > 100:
            raise ValueError("display_name must be at most 100 characters")
        if self.query_params and len(self.query_params) > 100:
            raise ValueError("query_params must be at most 100 characters")
        if self.parent_module and len(self.parent_module) > 50:
            raise ValueError("parent_module must be at most 50 characters")
        if self.service_code and len(self.service_code) > 50:
            raise ValueError("service_code must be at most 50 characters")
        if self.tenant_id and len(self.tenant_id) > 50:
            raise ValueError("tenant_id must be at most 50 characters")

    def to_dict(self) -> Dict[str, Any]:
        result = {}
        if self.id is not None:
            result["id"] = self.id
        if self.name:
            result["name"] = self.name
        if self.url:
            result["url"] = self.url
        if self.display_name:
            result["displayName"] = self.display_name
        if self.order_number is not None:
            result["orderNumber"] = self.order_number
        if self.query_params:
            result["queryParams"] = self.query_params
        if self.parent_module:
            result["parentModule"] = self.parent_module
        result["enabled"] = self.enabled
        if self.service_code:
            result["serviceCode"] = self.service_code
        if self.tenant_id:
            result["tenantId"] = self.tenant_id
        if self.created_date:
            result["createdDate"] = self.created_date.isoformat()
        if self.created_by is not None:
            result["createdBy"] = self.created_by
        if self.last_modified_date:
            result["lastModifiedDate"] = self.last_modified_date.isoformat()
        if self.last_modified_by is not None:
            result["lastModifiedBy"] = self.last_modified_by
        if self.path:
            result["path"] = self.path
        if self.navigation_url:
            result["navigationURL"] = self.navigation_url
        if self.left_icon:
            result["leftIcon"] = self.left_icon
        if self.right_icon:
            result["rightIcon"] = self.right_icon
        return result

@dataclass
class ActionSearchCriteria:
    """
    Model representing action search criteria
    """
    role_codes: Optional[List[str]] = field(default_factory=list)
    feature_ids: Optional[List[int]] = field(default_factory=list)
    tenant_id: Optional[str] = None

    def to_dict(self) -> Dict[str, Any]:
        result = {}
        if self.role_codes:
            result["roleCodes"] = self.role_codes
        if self.feature_ids:
            result["featureIds"] = self.feature_ids
        if self.tenant_id:
            result["tenantId"] = self.tenant_id
        return result

@dataclass
class ActionRequest:
    """
    Model representing an action request
    """
    request_info: Optional[RequestInfo] = None
    role_codes: Optional[List[str]] = field(default_factory=list)
    feature_ids: Optional[List[int]] = field(default_factory=list)
    tenant_id: Optional[str] = None
    enabled: Optional[bool] = None
    actions: Optional[List[Action]] = field(default_factory=list)
    action_master: Optional[str] = None
    navigation_url: Optional[str] = None
    left_icon: Optional[str] = None
    right_icon: Optional[str] = None

    def __post_init__(self):
        if self.tenant_id and len(self.tenant_id) > 50:
            raise ValueError("tenant_id must be at most 50 characters")

    def to_domain(self) -> ActionSearchCriteria:
        """
        Convert to ActionSearchCriteria domain object
        """
        return ActionSearchCriteria(
            role_codes=self.role_codes,
            feature_ids=self.feature_ids,
            tenant_id=self.tenant_id
        )

    def to_dict(self) -> Dict[str, Any]:
        result = {}
        if self.request_info:
            result["RequestInfo"] = self.request_info.to_dict()
        if self.role_codes:
            result["roleCodes"] = self.role_codes
        if self.feature_ids:
            result["featureIds"] = self.feature_ids
        if self.tenant_id:
            result["tenantId"] = self.tenant_id
        if self.enabled is not None:
            result["enabled"] = self.enabled
        if self.actions:
            result["actions"] = [action.to_dict() for action in self.actions]
        if self.action_master:
            result["actionMaster"] = self.action_master
        if self.navigation_url:
            result["navigationURL"] = self.navigation_url
        if self.left_icon:
            result["leftIcon"] = self.left_icon
        if self.right_icon:
            result["rightIcon"] = self.right_icon
        return result

class ActionBuilder:
    """Builder for creating Action objects"""
    def __init__(self):
        self._id: Optional[int] = None
        self._name: Optional[str] = None
        self._url: Optional[str] = None
        self._display_name: Optional[str] = None
        self._order_number: Optional[int] = None
        self._query_params: Optional[str] = None
        self._parent_module: Optional[str] = None
        self._enabled: bool = False
        self._service_code: Optional[str] = None
        self._tenant_id: Optional[str] = None
        self._created_date: Optional[datetime] = None
        self._created_by: Optional[int] = None
        self._last_modified_date: Optional[datetime] = None
        self._last_modified_by: Optional[int] = None
        self._path: Optional[str] = None
        self._navigation_url: Optional[str] = None
        self._left_icon: Optional[str] = None
        self._right_icon: Optional[str] = None

    def with_id(self, id: int) -> 'ActionBuilder':
        self._id = id
        return self

    def with_name(self, name: str) -> 'ActionBuilder':
        self._name = name
        return self

    def with_url(self, url: str) -> 'ActionBuilder':
        self._url = url
        return self

    def with_display_name(self, display_name: str) -> 'ActionBuilder':
        self._display_name = display_name
        return self

    def with_order_number(self, order_number: int) -> 'ActionBuilder':
        self._order_number = order_number
        return self

    def with_query_params(self, query_params: str) -> 'ActionBuilder':
        self._query_params = query_params
        return self

    def with_parent_module(self, parent_module: str) -> 'ActionBuilder':
        self._parent_module = parent_module
        return self

    def with_enabled(self, enabled: bool) -> 'ActionBuilder':
        self._enabled = enabled
        return self

    def with_service_code(self, service_code: str) -> 'ActionBuilder':
        self._service_code = service_code
        return self

    def with_tenant_id(self, tenant_id: str) -> 'ActionBuilder':
        self._tenant_id = tenant_id
        return self

    def with_created_date(self, created_date: datetime) -> 'ActionBuilder':
        self._created_date = created_date
        return self

    def with_created_by(self, created_by: int) -> 'ActionBuilder':
        self._created_by = created_by
        return self

    def with_last_modified_date(self, last_modified_date: datetime) -> 'ActionBuilder':
        self._last_modified_date = last_modified_date
        return self

    def with_last_modified_by(self, last_modified_by: int) -> 'ActionBuilder':
        self._last_modified_by = last_modified_by
        return self

    def with_path(self, path: str) -> 'ActionBuilder':
        self._path = path
        return self

    def with_navigation_url(self, navigation_url: str) -> 'ActionBuilder':
        self._navigation_url = navigation_url
        return self

    def with_left_icon(self, left_icon: str) -> 'ActionBuilder':
        self._left_icon = left_icon
        return self

    def with_right_icon(self, right_icon: str) -> 'ActionBuilder':
        self._right_icon = right_icon
        return self

    def build(self) -> Action:
        return Action(
            id=self._id,
            name=self._name,
            url=self._url,
            display_name=self._display_name,
            order_number=self._order_number,
            query_params=self._query_params,
            parent_module=self._parent_module,
            enabled=self._enabled,
            service_code=self._service_code,
            tenant_id=self._tenant_id,
            created_date=self._created_date,
            created_by=self._created_by,
            last_modified_date=self._last_modified_date,
            last_modified_by=self._last_modified_by,
            path=self._path,
            navigation_url=self._navigation_url,
            left_icon=self._left_icon,
            right_icon=self._right_icon
        )

class ActionRequestBuilder:
    """Builder for creating ActionRequest objects"""
    def __init__(self):
        self._request_info: Optional[RequestInfo] = None
        self._role_codes: List[str] = []
        self._feature_ids: List[int] = []
        self._tenant_id: Optional[str] = None
        self._enabled: Optional[bool] = None
        self._actions: List[Action] = []
        self._action_master: Optional[str] = None
        self._navigation_url: Optional[str] = None
        self._left_icon: Optional[str] = None
        self._right_icon: Optional[str] = None

    def with_request_info(self, request_info: RequestInfo) -> 'ActionRequestBuilder':
        self._request_info = request_info
        return self

    def with_role_codes(self, role_codes: List[str]) -> 'ActionRequestBuilder':
        self._role_codes = role_codes
        return self

    def add_role_code(self, role_code: str) -> 'ActionRequestBuilder':
        self._role_codes.append(role_code)
        return self

    def with_feature_ids(self, feature_ids: List[int]) -> 'ActionRequestBuilder':
        self._feature_ids = feature_ids
        return self

    def add_feature_id(self, feature_id: int) -> 'ActionRequestBuilder':
        self._feature_ids.append(feature_id)
        return self

    def with_tenant_id(self, tenant_id: str) -> 'ActionRequestBuilder':
        self._tenant_id = tenant_id
        return self

    def with_enabled(self, enabled: bool) -> 'ActionRequestBuilder':
        self._enabled = enabled
        return self

    def with_actions(self, actions: List[Action]) -> 'ActionRequestBuilder':
        self._actions = actions
        return self

    def add_action(self, action: Action) -> 'ActionRequestBuilder':
        self._actions.append(action)
        return self

    def with_action_master(self, action_master: str) -> 'ActionRequestBuilder':
        self._action_master = action_master
        return self

    def with_navigation_url(self, navigation_url: str) -> 'ActionRequestBuilder':
        self._navigation_url = navigation_url
        return self

    def with_left_icon(self, left_icon: str) -> 'ActionRequestBuilder':
        self._left_icon = left_icon
        return self

    def with_right_icon(self, right_icon: str) -> 'ActionRequestBuilder':
        self._right_icon = right_icon
        return self

    def build(self) -> ActionRequest:
        return ActionRequest(
            request_info=self._request_info,
            role_codes=self._role_codes,
            feature_ids=self._feature_ids,
            tenant_id=self._tenant_id,
            enabled=self._enabled,
            actions=self._actions,
            action_master=self._action_master,
            navigation_url=self._navigation_url,
            left_icon=self._left_icon,
            right_icon=self._right_icon
        )

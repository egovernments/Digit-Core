from dataclasses import dataclass, field
from typing import List, Optional, Set, Dict
from ..request_config import RequestInfo

@dataclass
class MasterDetail:
    """
    Model for master detail parameters
    """
    name: str
    filter: Optional[str] = None

    def __post_init__(self):
        if not self.name or len(self.name) > 100:
            raise ValueError("name must be between 1 and 100 characters")
        if self.filter and len(self.filter) > 500:
            raise ValueError("filter must be between 1 and 500 characters")

    def to_dict(self) -> dict:
        result = {'name': self.name}
        if self.filter:
            result['filter'] = self.filter
        return result

@dataclass
class ModuleDetail:
    """
    Model for module detail parameters
    """
    module_name: str
    master_details: List[MasterDetail] = field(default_factory=list)

    def __post_init__(self):
        if not self.module_name or len(self.module_name) > 100:
            raise ValueError("module_name must be between 1 and 100 characters")

    def add_master_details_item(self, master_details_item: MasterDetail) -> 'ModuleDetail':
        self.master_details.append(master_details_item)
        return self

    def to_dict(self) -> dict:
        return {
            'moduleName': self.module_name,
            'masterDetails': [master.to_dict() for master in self.master_details]
        }

@dataclass
class MdmsCriteria:
    """
    Model for MDMS criteria parameters
    """
    tenant_id: str
    module_details: List[ModuleDetail]
    ids: Optional[Set[str]] = None
    unique_identifier: Optional[str] = None
    schema_code_filter_map: Optional[Dict[str, str]] = field(default=None, repr=False)
    is_active: bool = field(default=True, repr=False)

    def __post_init__(self):
        if not self.tenant_id or len(self.tenant_id) > 100:
            raise ValueError("tenant_id must be between 1 and 100 characters")
        if self.unique_identifier and len(self.unique_identifier) > 64:
            raise ValueError("unique_identifier must be between 1 and 64 characters")

    def add_module_details_item(self, module_details_item: ModuleDetail) -> 'MdmsCriteria':
        if not self.module_details:
            self.module_details = []
        self.module_details.append(module_details_item)
        return self

    def to_dict(self) -> dict:
        result = {
            'tenantId': self.tenant_id,
            'moduleDetails': [module.to_dict() for module in self.module_details]
        }
        
        if self.ids:
            result['ids'] = list(self.ids)
        if self.unique_identifier:
            result['uniqueIdentifier'] = self.unique_identifier
            
        return result

class MdmsCriteriaBuilder:
    """Builder class for creating MdmsCriteria objects"""
    
    def __init__(self):
        self._tenant_id: Optional[str] = None
        self._module_details: List[ModuleDetail] = []
        self._ids: Optional[Set[str]] = None
        self._unique_identifier: Optional[str] = None
        self._schema_code_filter_map: Optional[Dict[str, str]] = None
        self._is_active: bool = True

    def with_tenant_id(self, tenant_id: str) -> 'MdmsCriteriaBuilder':
        self._tenant_id = tenant_id
        return self

    def with_module_details(self, module_details: List[ModuleDetail]) -> 'MdmsCriteriaBuilder':
        self._module_details = module_details
        return self

    def add_module_detail(self, module_detail: ModuleDetail) -> 'MdmsCriteriaBuilder':
        self._module_details.append(module_detail)
        return self

    def with_ids(self, ids: Set[str]) -> 'MdmsCriteriaBuilder':
        self._ids = ids
        return self

    def with_unique_identifier(self, unique_identifier: str) -> 'MdmsCriteriaBuilder':
        self._unique_identifier = unique_identifier
        return self

    def with_schema_code_filter_map(self, schema_code_filter_map: Dict[str, str]) -> 'MdmsCriteriaBuilder':
        self._schema_code_filter_map = schema_code_filter_map
        return self

    def with_is_active(self, is_active: bool) -> 'MdmsCriteriaBuilder':
        self._is_active = is_active
        return self

    def build(self) -> MdmsCriteria:
        if not self._tenant_id:
            raise ValueError("tenant_id is required")
        if not self._module_details:
            raise ValueError("module_details is required")
            
        return MdmsCriteria(
            tenant_id=self._tenant_id,
            module_details=self._module_details,
            ids=self._ids,
            unique_identifier=self._unique_identifier,
            schema_code_filter_map=self._schema_code_filter_map,
            is_active=self._is_active
        )

@dataclass
class MdmsCriteriaReq:
    """
    Model for MDMS criteria request parameters
    """
    request_info: Optional[RequestInfo] = None
    mdms_criteria: Optional[MdmsCriteria] = None

    def to_dict(self) -> dict:
        return {
            'RequestInfo': self.request_info.to_dict() if self.request_info else None,
            'MdmsCriteria': self.mdms_criteria.to_dict() if self.mdms_criteria else None
        }

class MdmsCriteriaReqBuilder:
    """Builder class for creating MdmsCriteriaReq objects"""
    
    def __init__(self):
        self._request_info: Optional[RequestInfo] = None
        self._mdms_criteria: Optional[MdmsCriteria] = None

    def with_request_info(self, request_info: RequestInfo) -> 'MdmsCriteriaReqBuilder':
        self._request_info = request_info
        return self

    def with_mdms_criteria(self, mdms_criteria: MdmsCriteria) -> 'MdmsCriteriaReqBuilder':
        self._mdms_criteria = mdms_criteria
        return self

    def build(self) -> MdmsCriteriaReq:
        return MdmsCriteriaReq(
            request_info=self._request_info,
            mdms_criteria=self._mdms_criteria
        )

class MasterDetailBuilder:
    """Builder class for creating MasterDetail objects"""
    
    def __init__(self):
        self._name: Optional[str] = None
        self._filter: Optional[str] = None

    def with_name(self, name: str) -> 'MasterDetailBuilder':
        self._name = name
        return self

    def with_filter(self, filter: str) -> 'MasterDetailBuilder':
        self._filter = filter
        return self

    def build(self) -> MasterDetail:
        if not self._name:
            raise ValueError("name is required")
        return MasterDetail(
            name=self._name,
            filter=self._filter
        )

class ModuleDetailBuilder:
    """Builder class for creating ModuleDetail objects"""
    
    def __init__(self):
        self._module_name: Optional[str] = None
        self._master_details: List[MasterDetail] = []

    def with_module_name(self, module_name: str) -> 'ModuleDetailBuilder':
        self._module_name = module_name
        return self

    def with_master_details(self, master_details: List[MasterDetail]) -> 'ModuleDetailBuilder':
        self._master_details = master_details
        return self

    def add_master_detail(self, master_detail: MasterDetail) -> 'ModuleDetailBuilder':
        self._master_details.append(master_detail)
        return self

    def build(self) -> ModuleDetail:
        if not self._module_name:
            raise ValueError("module_name is required")
        return ModuleDetail(
            module_name=self._module_name,
            master_details=self._master_details
        ) 
from dataclasses import dataclass, field
from typing import Optional, Dict, Any, List, Set
from ..request_config import RequestInfo

@dataclass
class AuditDetails:
    """
    Model for audit details
    """
    created_by: Optional[str] = None
    last_modified_by: Optional[str] = None
    created_time: Optional[int] = None
    last_modified_time: Optional[int] = None

    def to_dict(self) -> Dict[str, Any]:
        result = {}
        if self.created_by:
            result['createdBy'] = self.created_by
        if self.last_modified_by:
            result['lastModifiedBy'] = self.last_modified_by
        if self.created_time:
            result['createdTime'] = self.created_time
        if self.last_modified_time:
            result['lastModifiedTime'] = self.last_modified_time
        return result

@dataclass
class SchemaDefinition:
    """
    Model for schema definition
    """
    tenant_id: str
    code: str
    definition: Dict[str, Any]
    id: Optional[str] = None
    description: Optional[str] = None
    is_active: bool = True
    audit_details: Optional[AuditDetails] = None

    def __post_init__(self):
        if self.id and (len(self.id) < 2 or len(self.id) > 128):
            raise ValueError("id must be between 2 and 128 characters")
        if len(self.tenant_id) < 2 or len(self.tenant_id) > 128:
            raise ValueError("tenant_id must be between 2 and 128 characters")
        if len(self.code) < 2 or len(self.code) > 128:
            raise ValueError("code must be between 2 and 128 characters")
        if self.description and (len(self.description) < 2 or len(self.description) > 512):
            raise ValueError("description must be between 2 and 512 characters")

    def to_dict(self) -> Dict[str, Any]:
        result = {
            'tenantId': self.tenant_id,
            'code': self.code,
            'definition': self.definition,
            'isActive': self.is_active
        }
        if self.id:
            result['id'] = self.id
        if self.description:
            result['description'] = self.description
        if self.audit_details:
            result['auditDetails'] = self.audit_details.to_dict()
        return result

@dataclass
class SchemaDefinitionRequest:
    """
    Model for schema definition request
    """
    request_info: RequestInfo
    schema_definition: SchemaDefinition

    def to_dict(self) -> Dict[str, Any]:
        return {
            'RequestInfo': self.request_info.to_dict(),
            'SchemaDefinition': self.schema_definition.to_dict()
        }

@dataclass
class SchemaDefCriteria:
    """
    Model for schema definition search criteria
    """
    tenant_id: str
    codes: Optional[List[str]] = field(default_factory=list)
    offset: Optional[int] = None
    limit: Optional[int] = None

    def __post_init__(self):
        if not self.tenant_id or len(self.tenant_id) < 1 or len(self.tenant_id) > 100:
            raise ValueError("tenant_id is required and must be between 1 and 100 characters")

    def add_codes_item(self, code: str) -> 'SchemaDefCriteria':
        """
        Add a code to the codes list
        """
        if not self.codes:
            self.codes = []
        self.codes.append(code)
        return self

    def to_dict(self) -> Dict[str, Any]:
        result = {
            'tenantId': self.tenant_id
        }
        if self.codes:
            result['codes'] = self.codes
        if self.offset is not None:
            result['offset'] = self.offset
        if self.limit is not None:
            result['limit'] = self.limit
        return result

class AuditDetailsBuilder:
    """Builder class for creating AuditDetails objects"""
    def __init__(self):
        self._created_by: Optional[str] = None
        self._last_modified_by: Optional[str] = None
        self._created_time: Optional[int] = None
        self._last_modified_time: Optional[int] = None

    def with_created_by(self, created_by: str) -> 'AuditDetailsBuilder':
        self._created_by = created_by
        return self

    def with_last_modified_by(self, last_modified_by: str) -> 'AuditDetailsBuilder':
        self._last_modified_by = last_modified_by
        return self

    def with_created_time(self, created_time: int) -> 'AuditDetailsBuilder':
        self._created_time = created_time
        return self

    def with_last_modified_time(self, last_modified_time: int) -> 'AuditDetailsBuilder':
        self._last_modified_time = last_modified_time
        return self

    def build(self) -> AuditDetails:
        return AuditDetails(
            created_by=self._created_by,
            last_modified_by=self._last_modified_by,
            created_time=self._created_time,
            last_modified_time=self._last_modified_time
        )

class SchemaDefinitionBuilder:
    """Builder class for creating SchemaDefinition objects"""
    def __init__(self):
        self._id: Optional[str] = None
        self._tenant_id: Optional[str] = None
        self._code: Optional[str] = None
        self._description: Optional[str] = None
        self._definition: Optional[Dict[str, Any]] = None
        self._is_active: bool = True
        self._audit_details: Optional[AuditDetails] = None

    def with_id(self, id: str) -> 'SchemaDefinitionBuilder':
        self._id = id
        return self

    def with_tenant_id(self, tenant_id: str) -> 'SchemaDefinitionBuilder':
        self._tenant_id = tenant_id
        return self

    def with_code(self, code: str) -> 'SchemaDefinitionBuilder':
        self._code = code
        return self

    def with_description(self, description: str) -> 'SchemaDefinitionBuilder':
        self._description = description
        return self

    def with_definition(self, definition: Dict[str, Any]) -> 'SchemaDefinitionBuilder':
        self._definition = definition
        return self

    def with_is_active(self, is_active: bool) -> 'SchemaDefinitionBuilder':
        self._is_active = is_active
        return self

    def with_audit_details(self, audit_details: AuditDetails) -> 'SchemaDefinitionBuilder':
        self._audit_details = audit_details
        return self

    def build(self) -> SchemaDefinition:
        if not self._tenant_id:
            raise ValueError("tenant_id is required")
        if not self._code:
            raise ValueError("code is required")
        if not self._definition:
            raise ValueError("definition is required")
        
        return SchemaDefinition(
            id=self._id,
            tenant_id=self._tenant_id,
            code=self._code,
            description=self._description,
            definition=self._definition,
            is_active=self._is_active,
            audit_details=self._audit_details
        )

class SchemaDefinitionRequestBuilder:
    """Builder class for creating SchemaDefinitionRequest objects"""
    def __init__(self):
        self._request_info: Optional[RequestInfo] = None
        self._schema_definition: Optional[SchemaDefinition] = None

    def with_request_info(self, request_info: RequestInfo) -> 'SchemaDefinitionRequestBuilder':
        self._request_info = request_info
        return self

    def with_schema_definition(self, schema_definition: SchemaDefinition) -> 'SchemaDefinitionRequestBuilder':
        self._schema_definition = schema_definition
        return self

    def build(self) -> SchemaDefinitionRequest:
        if not self._request_info:
            raise ValueError("request_info is required")
        if not self._schema_definition:
            raise ValueError("schema_definition is required")
        
        return SchemaDefinitionRequest(
            request_info=self._request_info,
            schema_definition=self._schema_definition
        )

class SchemaDefCriteriaBuilder:
    """Builder class for creating SchemaDefCriteria objects"""
    def __init__(self):
        self._tenant_id: Optional[str] = None
        self._codes: List[str] = []
        self._offset: Optional[int] = None
        self._limit: Optional[int] = None

    def with_tenant_id(self, tenant_id: str) -> 'SchemaDefCriteriaBuilder':
        self._tenant_id = tenant_id
        return self

    def with_codes(self, codes: List[str]) -> 'SchemaDefCriteriaBuilder':
        self._codes = codes
        return self

    def add_code(self, code: str) -> 'SchemaDefCriteriaBuilder':
        self._codes.append(code)
        return self

    def with_offset(self, offset: int) -> 'SchemaDefCriteriaBuilder':
        self._offset = offset
        return self

    def with_limit(self, limit: int) -> 'SchemaDefCriteriaBuilder':
        self._limit = limit
        return self

    def build(self) -> SchemaDefCriteria:
        if not self._tenant_id:
            raise ValueError("tenant_id is required")
        
        return SchemaDefCriteria(
            tenant_id=self._tenant_id,
            codes=self._codes,
            offset=self._offset,
            limit=self._limit
        )

@dataclass
class Mdms:
    tenant_id: str
    schema_code: str
    data: Dict[str, Any]
    id: Optional[str] = None
    unique_identifier: Optional[str] = None
    is_active: bool = True
    audit_details: Optional[AuditDetails] = None

    def __post_init__(self):
        if self.id and (len(self.id) < 2 or len(self.id) > 64):
            raise ValueError("id must be between 2 and 64 characters")
        if len(self.tenant_id) < 2 or len(self.tenant_id) > 128:
            raise ValueError("tenant_id must be between 2 and 128 characters")
        if len(self.schema_code) < 2 or len(self.schema_code) > 128:
            raise ValueError("schema_code must be between 2 and 128 characters")
        if self.unique_identifier and (len(self.unique_identifier) < 1 or len(self.unique_identifier) > 128):
            raise ValueError("unique_identifier must be between 1 and 128 characters")

    def to_dict(self) -> Dict[str, Any]:
        result = {
            'tenantId': self.tenant_id,
            'schemaCode': self.schema_code,
            'data': self.data,
            'isActive': self.is_active
        }
        if self.id:
            result['id'] = self.id
        if self.unique_identifier:
            result['uniqueIdentifier'] = self.unique_identifier
        if self.audit_details:
            result['auditDetails'] = self.audit_details.to_dict()
        return result

@dataclass
class MdmsCriteriaV2:
    tenant_id: str
    ids: Optional[Set[str]] = field(default_factory=set)
    unique_identifiers: Optional[Set[str]] = field(default_factory=set)
    schema_code: Optional[str] = None
    filter_map: Optional[Dict[str, str]] = field(default_factory=dict)
    is_active: Optional[bool] = None
    schema_code_filter_map: Optional[Dict[str, str]] = field(default_factory=dict)
    unique_identifiers_for_ref_verification: Optional[Set[str]] = field(default_factory=set)
    offset: Optional[int] = None
    limit: Optional[int] = None

    def __post_init__(self):
        if not self.tenant_id or len(self.tenant_id) < 1 or len(self.tenant_id) > 100:
            raise ValueError("tenant_id is required and must be between 1 and 100 characters")
        if self.unique_identifiers and any(len(uid) < 1 or len(uid) > 64 for uid in self.unique_identifiers):
            raise ValueError("unique_identifiers must be between 1 and 64 characters")

    def to_dict(self) -> Dict[str, Any]:
        result = {
            'tenantId': self.tenant_id
        }
        if self.ids:
            result['ids'] = list(self.ids)
        if self.unique_identifiers:
            result['uniqueIdentifiers'] = list(self.unique_identifiers)
        if self.schema_code:
            result['schemaCode'] = self.schema_code
        if self.filter_map:
            result['filters'] = self.filter_map
        if self.is_active is not None:
            result['isActive'] = self.is_active
        if self.offset is not None:
            result['offset'] = self.offset
        if self.limit is not None:
            result['limit'] = self.limit
        return result

class MdmsBuilder:
    """Builder class for creating Mdms objects"""
    def __init__(self):
        self._id: Optional[str] = None
        self._tenant_id: Optional[str] = None
        self._schema_code: Optional[str] = None
        self._unique_identifier: Optional[str] = None
        self._data: Optional[Dict[str, Any]] = None
        self._is_active: bool = True
        self._audit_details: Optional[AuditDetails] = None

    def with_id(self, id: str) -> 'MdmsBuilder':
        self._id = id
        return self

    def with_tenant_id(self, tenant_id: str) -> 'MdmsBuilder':
        self._tenant_id = tenant_id
        return self

    def with_schema_code(self, schema_code: str) -> 'MdmsBuilder':
        self._schema_code = schema_code
        return self

    def with_unique_identifier(self, unique_identifier: str) -> 'MdmsBuilder':
        self._unique_identifier = unique_identifier
        return self

    def with_data(self, data: Dict[str, Any]) -> 'MdmsBuilder':
        self._data = data
        return self

    def with_is_active(self, is_active: bool) -> 'MdmsBuilder':
        self._is_active = is_active
        return self

    def with_audit_details(self, audit_details: AuditDetails) -> 'MdmsBuilder':
        self._audit_details = audit_details
        return self

    def build(self) -> Mdms:
        if not self._tenant_id:
            raise ValueError("tenant_id is required")
        if not self._schema_code:
            raise ValueError("schema_code is required")
        if not self._data:
            raise ValueError("data is required")
        
        return Mdms(
            id=self._id,
            tenant_id=self._tenant_id,
            schema_code=self._schema_code,
            unique_identifier=self._unique_identifier,
            data=self._data,
            is_active=self._is_active,
            audit_details=self._audit_details
        )

class MdmsCriteriaV2Builder:
    """Builder class for creating MdmsCriteriaV2 objects"""
    def __init__(self):
        self._tenant_id: Optional[str] = None
        self._ids: Set[str] = set()
        self._unique_identifiers: Set[str] = set()
        self._schema_code: Optional[str] = None
        self._filter_map: Dict[str, str] = {}
        self._is_active: Optional[bool] = None
        self._schema_code_filter_map: Dict[str, str] = {}
        self._unique_identifiers_for_ref_verification: Set[str] = set()
        self._offset: Optional[int] = None
        self._limit: Optional[int] = None

    def with_tenant_id(self, tenant_id: str) -> 'MdmsCriteriaV2Builder':
        self._tenant_id = tenant_id
        return self

    def with_ids(self, ids: Set[str]) -> 'MdmsCriteriaV2Builder':
        self._ids = ids
        return self

    def with_unique_identifiers(self, unique_identifiers: Set[str]) -> 'MdmsCriteriaV2Builder':
        self._unique_identifiers = unique_identifiers
        return self

    def with_schema_code(self, schema_code: str) -> 'MdmsCriteriaV2Builder':
        self._schema_code = schema_code
        return self

    def with_filter_map(self, filter_map: Dict[str, str]) -> 'MdmsCriteriaV2Builder':
        self._filter_map = filter_map
        return self

    def with_is_active(self, is_active: bool) -> 'MdmsCriteriaV2Builder':
        self._is_active = is_active
        return self

    def with_schema_code_filter_map(self, schema_code_filter_map: Dict[str, str]) -> 'MdmsCriteriaV2Builder':
        self._schema_code_filter_map = schema_code_filter_map
        return self

    def with_unique_identifiers_for_ref_verification(self, unique_identifiers_for_ref_verification: Set[str]) -> 'MdmsCriteriaV2Builder':
        self._unique_identifiers_for_ref_verification = unique_identifiers_for_ref_verification
        return self

    def with_offset(self, offset: int) -> 'MdmsCriteriaV2Builder':
        self._offset = offset
        return self

    def with_limit(self, limit: int) -> 'MdmsCriteriaV2Builder':
        self._limit = limit
        return self

    def build(self) -> MdmsCriteriaV2:
        if not self._tenant_id:
            raise ValueError("tenant_id is required")
        
        return MdmsCriteriaV2(
            tenant_id=self._tenant_id,
            ids=self._ids,
            unique_identifiers=self._unique_identifiers,
            schema_code=self._schema_code,
            filter_map=self._filter_map,
            is_active=self._is_active,
            schema_code_filter_map=self._schema_code_filter_map,
            unique_identifiers_for_ref_verification=self._unique_identifiers_for_ref_verification,
            offset=self._offset,
            limit=self._limit
        )
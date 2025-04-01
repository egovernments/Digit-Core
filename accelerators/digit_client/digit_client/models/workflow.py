from dataclasses import dataclass, field
from typing import List, Optional, Any, Dict
from ..request_config import RequestInfo
from .user import User
from .mdms_v2 import AuditDetails

@dataclass
class Document:
    """
    Model for document details
    """
    id: Optional[str] = None
    tenant_id: Optional[str] = None
    document_type: Optional[str] = None
    file_store_id: Optional[str] = None
    document_uid: Optional[str] = None
    audit_details: Optional[AuditDetails] = None

    def __post_init__(self):
        if self.id and len(self.id) > 64:
            raise ValueError("id must be at most 64 characters")
        if self.tenant_id and len(self.tenant_id) > 64:
            raise ValueError("tenant_id must be at most 64 characters")
        if self.document_type and len(self.document_type) > 64:
            raise ValueError("document_type must be at most 64 characters")
        if self.file_store_id and len(self.file_store_id) > 64:
            raise ValueError("file_store_id must be at most 64 characters")
        if self.document_uid and len(self.document_uid) > 64:
            raise ValueError("document_uid must be at most 64 characters")

    def to_dict(self) -> Dict[str, Any]:
        result = {}
        if self.id:
            result['id'] = self.id
        if self.tenant_id:
            result['tenantId'] = self.tenant_id
        if self.document_type:
            result['documentType'] = self.document_type
        if self.file_store_id:
            result['fileStoreId'] = self.file_store_id
        if self.document_uid:
            result['documentUid'] = self.document_uid
        if self.audit_details:
            result['auditDetails'] = self.audit_details.to_dict()
        return result

@dataclass
class WorkflowAction:
    """
    Model for action details
    """
    uuid: Optional[str] = None
    tenant_id: Optional[str] = None
    current_state: Optional[str] = None
    action: str = field(default="")
    next_state: str = field(default="")
    roles: List[str] = field(default_factory=list)
    audit_details: Optional[AuditDetails] = None
    active: Optional[bool] = None

    def __post_init__(self):
        if self.uuid and len(self.uuid) > 256:
            raise ValueError("uuid must be at most 256 characters")
        if self.tenant_id and len(self.tenant_id) > 256:
            raise ValueError("tenant_id must be at most 256 characters")
        if self.current_state and len(self.current_state) > 256:
            raise ValueError("current_state must be at most 256 characters")
        if not self.action:
            raise ValueError("action is required")
        if len(self.action) > 256:
            raise ValueError("action must be at most 256 characters")
        if not self.next_state:
            raise ValueError("next_state is required")
        if len(self.next_state) > 256:
            raise ValueError("next_state must be at most 256 characters")
        if not self.roles:
            raise ValueError("roles is required")
        if len(self.roles) > 1024:
            raise ValueError("roles must be at most 1024 items")

    def add_roles_item(self, roles_item: str) -> 'Action':
        if self.roles is None:
            self.roles = []
        self.roles.append(roles_item)
        return self

    def to_dict(self) -> Dict[str, Any]:
        result = {
            'action': self.action,
            'nextState': self.next_state,
            'roles': self.roles
        }
        if self.uuid:
            result['uuid'] = self.uuid
        if self.tenant_id:
            result['tenantId'] = self.tenant_id
        if self.current_state:
            result['currentState'] = self.current_state
        if self.audit_details:
            result['auditDetails'] = self.audit_details.to_dict()
        if self.active is not None:
            result['active'] = self.active
        return result

@dataclass
class State:
    """
    Model for state details
    """
    uuid: Optional[str] = None
    tenant_id: Optional[str] = None
    business_service_id: Optional[str] = None
    sla: Optional[int] = None
    state: Optional[str] = None
    application_status: Optional[str] = None
    doc_upload_required: Optional[bool] = None
    is_start_state: Optional[bool] = None
    is_terminate_state: Optional[bool] = None
    is_state_updatable: Optional[bool] = None
    actions: List[WorkflowAction] = field(default_factory=list)
    audit_details: Optional[AuditDetails] = None

    def __post_init__(self):
        if self.uuid and len(self.uuid) > 256:
            raise ValueError("uuid must be at most 256 characters")
        if self.tenant_id and len(self.tenant_id) > 256:
            raise ValueError("tenant_id must be at most 256 characters")
        if self.business_service_id and len(self.business_service_id) > 256:
            raise ValueError("business_service_id must be at most 256 characters")
        if self.state and len(self.state) > 256:
            raise ValueError("state must be at most 256 characters")
        if self.application_status and len(self.application_status) > 256:
            raise ValueError("application_status must be at most 256 characters")

    def add_actions_item(self, actions_item: WorkflowAction) -> 'State':
        if self.actions is None:
            self.actions = []
        self.actions.append(actions_item)
        return self

    def to_dict(self) -> Dict[str, Any]:
        result = {}
        if self.uuid:
            result['uuid'] = self.uuid
        if self.tenant_id:
            result['tenantId'] = self.tenant_id
        if self.business_service_id:
            result['businessServiceId'] = self.business_service_id
        if self.sla is not None:
            result['sla'] = self.sla
        if self.state:
            result['state'] = self.state
        if self.application_status:
            result['applicationStatus'] = self.application_status
        if self.doc_upload_required is not None:
            result['docUploadRequired'] = self.doc_upload_required
        if self.is_start_state is not None:
            result['isStartState'] = self.is_start_state
        if self.is_terminate_state is not None:
            result['isTerminateState'] = self.is_terminate_state
        if self.is_state_updatable is not None:
            result['isStateUpdatable'] = self.is_state_updatable
        if self.actions:
            result['actions'] = [action.to_dict() for action in self.actions]
        if self.audit_details:
            result['auditDetails'] = self.audit_details.to_dict()
        return result

@dataclass
class ProcessInstance:
    """
    Model for process instance
    """
    tenant_id: str
    business_service: str
    business_id: str
    action: str
    module_name: str
    id: Optional[str] = None
    state: Optional[State] = None
    comment: Optional[str] = None
    documents: List[Document] = field(default_factory=list)
    assigner: Optional[User] = None
    assignes: List[User] = field(default_factory=list)
    next_actions: List[WorkflowAction] = field(default_factory=list)
    state_sla: Optional[int] = None
    business_service_sla: Optional[int] = None
    previous_status: Optional[str] = None
    entity: Optional[Any] = None
    audit_details: Optional[AuditDetails] = None
    rating: Optional[int] = None
    escalated: bool = False

    def __post_init__(self):
        if self.id and len(self.id) > 64:
            raise ValueError("id must be at most 64 characters")
        if not self.tenant_id:
            raise ValueError("tenant_id is required")
        if len(self.tenant_id) > 128:
            raise ValueError("tenant_id must be at most 128 characters")
        if not self.business_service:
            raise ValueError("business_service is required")
        if len(self.business_service) > 128:
            raise ValueError("business_service must be at most 128 characters")
        if not self.business_id:
            raise ValueError("business_id is required")
        if len(self.business_id) > 128:
            raise ValueError("business_id must be at most 128 characters")
        if not self.action:
            raise ValueError("action is required")
        if len(self.action) > 128:
            raise ValueError("action must be at most 128 characters")
        if not self.module_name:
            raise ValueError("module_name is required")
        if len(self.module_name) > 64:
            raise ValueError("module_name must be at most 64 characters")
        if self.comment and len(self.comment) > 1024:
            raise ValueError("comment must be at most 1024 characters")
        if self.previous_status and len(self.previous_status) > 128:
            raise ValueError("previous_status must be at most 128 characters")

    def add_documents_item(self, documents_item: Document) -> 'ProcessInstance':
        if self.documents is None:
            self.documents = []
        if documents_item not in self.documents:
            self.documents.append(documents_item)
        return self

    def add_next_actions_item(self, next_actions_item: WorkflowAction) -> 'ProcessInstance':
        if self.next_actions is None:
            self.next_actions = []
        self.next_actions.append(next_actions_item)
        return self

    def add_users_item(self, users_item: User) -> 'ProcessInstance':
        if self.assignes is None:
            self.assignes = []
        if users_item not in self.assignes:
            self.assignes.append(users_item)
        return self

    def to_dict(self) -> Dict[str, Any]:
        result = {
            'tenantId': self.tenant_id,
            'businessService': self.business_service,
            'businessId': self.business_id,
            'action': self.action,
            'moduleName': self.module_name
        }
        if self.id:
            result['id'] = self.id
        if self.state:
            result['state'] = self.state.to_dict()
        if self.comment:
            result['comment'] = self.comment
        if self.documents:
            result['documents'] = [doc.to_dict() for doc in self.documents]
        if self.assigner:
            result['assigner'] = self.assigner.to_dict()
        if self.assignes:
            result['assignes'] = [user.to_dict() for user in self.assignes]
        if self.next_actions:
            result['nextActions'] = [action.to_dict() for action in self.next_actions]
        if self.state_sla is not None:
            result['stateSla'] = self.state_sla
        if self.business_service_sla is not None:
            result['businesssServiceSla'] = self.business_service_sla
        if self.previous_status:
            result['previousStatus'] = self.previous_status
        if self.entity:
            result['entity'] = self.entity
        if self.audit_details:
            result['auditDetails'] = self.audit_details.to_dict()
        if self.rating is not None:
            result['rating'] = self.rating
        result['escalated'] = self.escalated
        return result

# Builder classes
class DocumentBuilder:
    """Builder for creating Document objects"""
    def __init__(self):
        self._id: Optional[str] = None
        self._tenant_id: Optional[str] = None
        self._document_type: Optional[str] = None
        self._file_store_id: Optional[str] = None
        self._document_uid: Optional[str] = None
        self._audit_details: Optional[AuditDetails] = None

    def with_id(self, id: str) -> 'DocumentBuilder':
        self._id = id
        return self

    def with_tenant_id(self, tenant_id: str) -> 'DocumentBuilder':
        self._tenant_id = tenant_id
        return self

    def with_document_type(self, document_type: str) -> 'DocumentBuilder':
        self._document_type = document_type
        return self

    def with_file_store_id(self, file_store_id: str) -> 'DocumentBuilder':
        self._file_store_id = file_store_id
        return self

    def with_document_uid(self, document_uid: str) -> 'DocumentBuilder':
        self._document_uid = document_uid
        return self

    def with_audit_details(self, audit_details: AuditDetails) -> 'DocumentBuilder':
        self._audit_details = audit_details
        return self

    def build(self) -> Document:
        return Document(
            id=self._id,
            tenant_id=self._tenant_id,
            document_type=self._document_type,
            file_store_id=self._file_store_id,
            document_uid=self._document_uid,
            audit_details=self._audit_details
        )

class WorkflowActionBuilder:
    """Builder for creating Action objects"""
    def __init__(self):
        self._uuid: Optional[str] = None
        self._tenant_id: Optional[str] = None
        self._current_state: Optional[str] = None
        self._action: Optional[str] = None
        self._next_state: Optional[str] = None
        self._roles: List[str] = []
        self._audit_details: Optional[AuditDetails] = None
        self._active: Optional[bool] = None

    def with_uuid(self, uuid: str) -> 'ActionBuilder':
        self._uuid = uuid
        return self

    def with_tenant_id(self, tenant_id: str) -> 'ActionBuilder':
        self._tenant_id = tenant_id
        return self

    def with_current_state(self, current_state: str) -> 'ActionBuilder':
        self._current_state = current_state
        return self

    def with_action(self, action: str) -> 'ActionBuilder':
        self._action = action
        return self

    def with_next_state(self, next_state: str) -> 'ActionBuilder':
        self._next_state = next_state
        return self

    def with_roles(self, roles: List[str]) -> 'ActionBuilder':
        self._roles = roles
        return self

    def add_role(self, role: str) -> 'ActionBuilder':
        self._roles.append(role)
        return self

    def with_audit_details(self, audit_details: AuditDetails) -> 'ActionBuilder':
        self._audit_details = audit_details
        return self

    def with_active(self, active: bool) -> 'ActionBuilder':
        self._active = active
        return self

    def build(self) -> WorkflowAction:
        if not self._action:
            raise ValueError("action is required")
        if not self._next_state:
            raise ValueError("next_state is required")
        if not self._roles:
            raise ValueError("roles is required")
            
        return WorkflowAction(
            uuid=self._uuid,
            tenant_id=self._tenant_id,
            current_state=self._current_state,
            action=self._action,
            next_state=self._next_state,
            roles=self._roles,
            audit_details=self._audit_details,
            active=self._active
        )

class StateBuilder:
    """Builder for creating State objects"""
    def __init__(self):
        self._uuid: Optional[str] = None
        self._tenant_id: Optional[str] = None
        self._business_service_id: Optional[str] = None
        self._sla: Optional[int] = None
        self._state: Optional[str] = None
        self._application_status: Optional[str] = None
        self._doc_upload_required: Optional[bool] = None
        self._is_start_state: Optional[bool] = None
        self._is_terminate_state: Optional[bool] = None
        self._is_state_updatable: Optional[bool] = None
        self._actions: List[WorkflowAction] = []
        self._audit_details: Optional[AuditDetails] = None

    def with_uuid(self, uuid: str) -> 'StateBuilder':
        self._uuid = uuid
        return self

    def with_tenant_id(self, tenant_id: str) -> 'StateBuilder':
        self._tenant_id = tenant_id
        return self

    def with_business_service_id(self, business_service_id: str) -> 'StateBuilder':
        self._business_service_id = business_service_id
        return self

    def with_sla(self, sla: int) -> 'StateBuilder':
        self._sla = sla
        return self

    def with_state(self, state: str) -> 'StateBuilder':
        self._state = state
        return self

    def with_application_status(self, application_status: str) -> 'StateBuilder':
        self._application_status = application_status
        return self

    def with_doc_upload_required(self, doc_upload_required: bool) -> 'StateBuilder':
        self._doc_upload_required = doc_upload_required
        return self

    def with_is_start_state(self, is_start_state: bool) -> 'StateBuilder':
        self._is_start_state = is_start_state
        return self

    def with_is_terminate_state(self, is_terminate_state: bool) -> 'StateBuilder':
        self._is_terminate_state = is_terminate_state
        return self

    def with_is_state_updatable(self, is_state_updatable: bool) -> 'StateBuilder':
        self._is_state_updatable = is_state_updatable
        return self

    def with_actions(self, actions: List[WorkflowAction]) -> 'StateBuilder':
        self._actions = actions
        return self
    
    def with_audit_details(self, audit_details: AuditDetails) -> 'StateBuilder':
        self._audit_details = audit_details
        return self

    def build(self) -> State:
        return State(
            uuid=self._uuid,
            tenant_id=self._tenant_id,
            business_service_id=self._business_service_id,
            sla=self._sla,
            state=self._state,
            application_status=self._application_status,
            doc_upload_required=self._doc_upload_required,
            is_start_state=self._is_start_state,
            is_terminate_state=self._is_terminate_state,
            is_state_updatable=self._is_state_updatable,
            actions=self._actions,
            audit_details=self._audit_details
        )

class ProcessInstanceBuilder:
    """Builder for creating ProcessInstance objects"""  
    def __init__(self):
        self._tenant_id: Optional[str] = None
        self._business_service: Optional[str] = None
        self._business_id: Optional[str] = None
        self._action: Optional[str] = None
        self._module_name: Optional[str] = None
        self._id: Optional[str] = None
        self._state: Optional[State] = None
        self._comment: Optional[str] = None
        self._documents: List[Document] = []
        self._assigner: Optional[User] = None
        self._assignes: List[User] = []
        self._next_actions: List[WorkflowAction] = []
        self._state_sla: Optional[int] = None
        self._business_service_sla: Optional[int] = None
        self._previous_status: Optional[str] = None
        self._entity: Optional[Any] = None
        self._audit_details: Optional[AuditDetails] = None
        self._rating: Optional[int] = None
        self._escalated: Optional[bool] = None

    def with_tenant_id(self, tenant_id: str) -> 'ProcessInstanceBuilder':
        self._tenant_id = tenant_id
        return self

    def with_business_service(self, business_service: str) -> 'ProcessInstanceBuilder':
        self._business_service = business_service
        return self

    def with_business_id(self, business_id: str) -> 'ProcessInstanceBuilder':
        self._business_id = business_id
        return self

    def with_action(self, action: str) -> 'ProcessInstanceBuilder':
        self._action = action
        return self 
    
    def with_module_name(self, module_name: str) -> 'ProcessInstanceBuilder':
        self._module_name = module_name
        return self
    
    def with_id(self, id: str) -> 'ProcessInstanceBuilder': 
        self._id = id
        return self
    
    def with_state(self, state: State) -> 'ProcessInstanceBuilder':
        self._state = state
        return self 
    
    def with_comment(self, comment: str) -> 'ProcessInstanceBuilder':
        self._comment = comment
        return self
    
    def with_documents(self, documents: List[Document]) -> 'ProcessInstanceBuilder':    
        self._documents = documents
        return self
    
    def with_assigner(self, assigner: User) -> 'ProcessInstanceBuilder':
        self._assigner = assigner
        return self 
    
    def with_assignes(self, assignes: List[User]) -> 'ProcessInstanceBuilder':
        self._assignes = assignes
        return self
    
    def with_next_actions(self, next_actions: List[WorkflowAction]) -> 'ProcessInstanceBuilder':    
        self._next_actions = next_actions
        return self
    
    def with_state_sla(self, state_sla: int) -> 'ProcessInstanceBuilder':
        self._state_sla = state_sla
        return self 
    
    def with_business_service_sla(self, business_service_sla: int) -> 'ProcessInstanceBuilder':
        self._business_service_sla = business_service_sla
        return self
    
    def with_previous_status(self, previous_status: str) -> 'ProcessInstanceBuilder':       
        self._previous_status = previous_status
        return self
    
    def with_entity(self, entity: Any) -> 'ProcessInstanceBuilder':
        self._entity = entity
        return self 
    
    def with_audit_details(self, audit_details: AuditDetails) -> 'ProcessInstanceBuilder':
        self._audit_details = audit_details
        return self
    
    def with_rating(self, rating: int) -> 'ProcessInstanceBuilder': 
        self._rating = rating
        return self
    
    def with_escalated(self, escalated: bool) -> 'ProcessInstanceBuilder':
        self._escalated = escalated
        return self 
    
    def build(self) -> ProcessInstance:
        return ProcessInstance(
            tenant_id=self._tenant_id,
            business_service=self._business_service,
            business_id=self._business_id,  
            action=self._action,
            module_name=self._module_name,
            id=self._id,
            state=self._state,
            comment=self._comment,
            documents=self._documents,  
            assigner=self._assigner,
            assignes=self._assignes,
            next_actions=self._next_actions,
            state_sla=self._state_sla,
            business_service_sla=self._business_service_sla,
            previous_status=self._previous_status,  
            entity=self._entity,
            audit_details=self._audit_details,
            rating=self._rating,
            escalated=self._escalated
        )       
        
@dataclass
class ProcessInstanceSearchCriteria:
    """
    Model for process instance search criteria
    """
    tenant_id: str
    status: Optional[List[str]] = None
    business_ids: Optional[List[str]] = None
    assignee: Optional[str] = None
    ids: Optional[List[str]] = None
    history: bool = False
    from_date: Optional[int] = None
    to_date: Optional[int] = None
    offset: Optional[int] = None
    limit: Optional[int] = None
    business_service: Optional[str] = None
    module_name: Optional[str] = None
    is_nearing_sla_count: Optional[bool] = field(default=None, repr=False)
    tenant_specific_status: Optional[List[str]] = field(default=None, repr=False)
    multiple_assignees: Optional[List[str]] = field(default=None, repr=False)
    states_to_ignore: Optional[List[str]] = field(default=None, repr=False)
    is_escalated_count: Optional[bool] = field(default=None, repr=False)
    is_assigned_to_me_count: Optional[bool] = field(default=None, repr=False)
    statuses_irrespective_of_tenant: Optional[List[str]] = field(default=None, repr=False)
    slot_percentage_sla_limit: Optional[int] = field(default=None, repr=False)

    def __post_init__(self):
        if not self.tenant_id:
            raise ValueError("tenant_id is required")
        if self.business_ids:
            for business_id in self.business_ids:
                if len(business_id) < 4:
                    raise ValueError("business_id must be at least 4 characters")

    def is_null(self) -> bool:
        """
        Check if the main search criteria are null
        """
        return (self.business_ids is None and self.ids is None and 
                self.assignee is None and self.status is None)

    def to_dict(self) -> Dict[str, Any]:
        result = {
            'tenantId': self.tenant_id
        }
        if self.status:
            result['status'] = self.status
        if self.business_ids:
            result['businessIds'] = self.business_ids
        if self.assignee:
            result['assignee'] = self.assignee
        if self.ids:
            result['ids'] = self.ids
        if self.history is not None:
            result['history'] = self.history
        if self.from_date is not None:
            result['fromDate'] = self.from_date
        if self.to_date is not None:
            result['toDate'] = self.to_date
        if self.offset is not None:
            result['offset'] = self.offset
        if self.limit is not None:
            result['limit'] = self.limit
        if self.business_service:
            result['businessService'] = self.business_service
        if self.module_name:
            result['moduleName'] = self.module_name
        return result

class ProcessInstanceSearchCriteriaBuilder:
    """Builder for creating ProcessInstanceSearchCriteria objects"""
    def __init__(self):
        self._tenant_id: Optional[str] = None
        self._status: Optional[List[str]] = None
        self._business_ids: Optional[List[str]] = None
        self._assignee: Optional[str] = None
        self._ids: Optional[List[str]] = None
        self._history: bool = False
        self._from_date: Optional[int] = None
        self._to_date: Optional[int] = None
        self._offset: Optional[int] = None
        self._limit: Optional[int] = None
        self._business_service: Optional[str] = None
        self._module_name: Optional[str] = None
        self._is_nearing_sla_count: Optional[bool] = None
        self._tenant_specific_status: Optional[List[str]] = None
        self._multiple_assignees: Optional[List[str]] = None
        self._states_to_ignore: Optional[List[str]] = None
        self._is_escalated_count: Optional[bool] = None
        self._is_assigned_to_me_count: Optional[bool] = None
        self._statuses_irrespective_of_tenant: Optional[List[str]] = None
        self._slot_percentage_sla_limit: Optional[int] = None

    def with_tenant_id(self, tenant_id: str) -> 'ProcessInstanceSearchCriteriaBuilder':
        self._tenant_id = tenant_id
        return self

    def with_status(self, status: List[str]) -> 'ProcessInstanceSearchCriteriaBuilder':
        self._status = status
        return self

    def with_business_ids(self, business_ids: List[str]) -> 'ProcessInstanceSearchCriteriaBuilder':
        self._business_ids = business_ids
        return self

    def with_assignee(self, assignee: str) -> 'ProcessInstanceSearchCriteriaBuilder':
        self._assignee = assignee
        return self

    def with_ids(self, ids: List[str]) -> 'ProcessInstanceSearchCriteriaBuilder':
        self._ids = ids
        return self

    def with_history(self, history: bool) -> 'ProcessInstanceSearchCriteriaBuilder':
        self._history = history
        return self

    def with_from_date(self, from_date: int) -> 'ProcessInstanceSearchCriteriaBuilder':
        self._from_date = from_date
        return self

    def with_to_date(self, to_date: int) -> 'ProcessInstanceSearchCriteriaBuilder':
        self._to_date = to_date
        return self

    def with_offset(self, offset: int) -> 'ProcessInstanceSearchCriteriaBuilder':
        self._offset = offset
        return self

    def with_limit(self, limit: int) -> 'ProcessInstanceSearchCriteriaBuilder':
        self._limit = limit
        return self

    def with_business_service(self, business_service: str) -> 'ProcessInstanceSearchCriteriaBuilder':
        self._business_service = business_service
        return self

    def with_module_name(self, module_name: str) -> 'ProcessInstanceSearchCriteriaBuilder':
        self._module_name = module_name
        return self

    def with_is_nearing_sla_count(self, is_nearing_sla_count: bool) -> 'ProcessInstanceSearchCriteriaBuilder':
        self._is_nearing_sla_count = is_nearing_sla_count
        return self

    def with_tenant_specific_status(self, tenant_specific_status: List[str]) -> 'ProcessInstanceSearchCriteriaBuilder':
        self._tenant_specific_status = tenant_specific_status
        return self

    def with_multiple_assignees(self, multiple_assignees: List[str]) -> 'ProcessInstanceSearchCriteriaBuilder':
        self._multiple_assignees = multiple_assignees
        return self

    def with_states_to_ignore(self, states_to_ignore: List[str]) -> 'ProcessInstanceSearchCriteriaBuilder':
        self._states_to_ignore = states_to_ignore
        return self

    def with_is_escalated_count(self, is_escalated_count: bool) -> 'ProcessInstanceSearchCriteriaBuilder':
        self._is_escalated_count = is_escalated_count
        return self

    def with_is_assigned_to_me_count(self, is_assigned_to_me_count: bool) -> 'ProcessInstanceSearchCriteriaBuilder':
        self._is_assigned_to_me_count = is_assigned_to_me_count
        return self

    def with_statuses_irrespective_of_tenant(self, statuses_irrespective_of_tenant: List[str]) -> 'ProcessInstanceSearchCriteriaBuilder':
        self._statuses_irrespective_of_tenant = statuses_irrespective_of_tenant
        return self

    def with_slot_percentage_sla_limit(self, slot_percentage_sla_limit: int) -> 'ProcessInstanceSearchCriteriaBuilder':
        self._slot_percentage_sla_limit = slot_percentage_sla_limit
        return self

    def build(self) -> ProcessInstanceSearchCriteria:
        if not self._tenant_id:
            raise ValueError("tenant_id is required")
        
        return ProcessInstanceSearchCriteria(
            tenant_id=self._tenant_id,
            status=self._status,
            business_ids=self._business_ids,
            assignee=self._assignee,
            ids=self._ids,
            history=self._history,
            from_date=self._from_date,
            to_date=self._to_date,
            offset=self._offset,
            limit=self._limit,
            business_service=self._business_service,
            module_name=self._module_name,
            is_nearing_sla_count=self._is_nearing_sla_count,
            tenant_specific_status=self._tenant_specific_status,
            multiple_assignees=self._multiple_assignees,
            states_to_ignore=self._states_to_ignore,
            is_escalated_count=self._is_escalated_count,
            is_assigned_to_me_count=self._is_assigned_to_me_count,
            statuses_irrespective_of_tenant=self._statuses_irrespective_of_tenant,
            slot_percentage_sla_limit=self._slot_percentage_sla_limit
        )

        
        



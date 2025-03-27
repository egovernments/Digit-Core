"""
DIGIT Client Library for Python
"""

__version__ = "0.1.0"

from .api_client import APIClient
from .config import Config
from .services import AuthenticationService, UserService, MDMSService, MDMSV2Service, AuthorizeService
from .request_config import RequestConfig, RequestInfo, RequestInfoBuilder
from .models.citizen_user import CitizenUser, Role, CitizenUserBuilder
from .models.search_models import UserSearchModel
from .models.user_profile import UserProfileUpdate, UserRole,UserProfileUpdateBuilder
from .models.auth import AuthenticationRequest, AuthenticationRequestBuilder
from .models.mdms import (
    MdmsCriteriaReq, 
    MdmsCriteriaReqBuilder,
    MdmsCriteria,
    MdmsCriteriaBuilder,
    ModuleDetail,
    ModuleDetailBuilder,
    MasterDetail,
    MasterDetailBuilder
)
from .models.mdms_v2 import (
    SchemaDefinition,
    SchemaDefinitionBuilder,
    SchemaDefCriteria,
    SchemaDefCriteriaBuilder,
    AuditDetails,
    AuditDetailsBuilder,
    Mdms,
    MdmsBuilder,
    MdmsCriteriaV2,
    MdmsCriteriaV2Builder,
    
)
from .models.AuthorizationRequest import AuthorizationRequest, AuthorizationRequestBuilder, Role, RoleBuilder

__all__ = [
    'APIClient',
    'Config',
    'AuthenticationService',
    'UserService',
    'MDMSService',
    'MDMSV2Service',
    'AuthorizeService',
    'RequestConfig',
    'RequestInfo',
    'RequestInfoBuilder',
    'CitizenUser',
    'Role',
    'UserSearchModel',
    'UserProfileUpdate',
    'UserRole',
    'UserProfileUpdateBuilder',
    'CitizenUserBuilder',
    'AuthenticationRequest',
    'AuthenticationRequestBuilder',
    'MdmsCriteriaReq',
    'MdmsCriteriaReqBuilder',
    'MdmsCriteria',
    'MdmsCriteriaBuilder',
    'ModuleDetail',
    'ModuleDetailBuilder',
    'MasterDetail',
    'MasterDetailBuilder',
    'SchemaDefinition',
    'SchemaDefinitionBuilder',
    'SchemaDefCriteria',
    'SchemaDefCriteriaBuilder',
    'AuditDetails',
    'AuditDetailsBuilder',
    'Mdms',
    'MdmsBuilder',
    'MdmsCriteriaV2',
    'MdmsCriteriaV2Builder',
    'AuthorizationRequest',
    'AuthorizationRequestBuilder',
    'Role',
    'RoleBuilder',
] 
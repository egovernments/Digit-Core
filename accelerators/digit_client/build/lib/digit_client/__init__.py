"""
DIGIT Client Library for Python
"""

__version__ = "0.1.0"

from .api_client import APIClient
from .config import Config
from .services import AuthenticationService, UserService, MDMSService
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

__all__ = [
    'APIClient',
    'Config',
    'AuthenticationService',
    'UserService',
    'MDMSService',
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
    'MasterDetailBuilder'
] 
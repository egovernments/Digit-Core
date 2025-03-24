from typing import Optional, Dict
import time
import uuid

class RequestInfo:
    def __init__(self, api_id: str, ver: str, ts: int, action: str,
                 did: str = None, key: str = None, msg_id: str = None,
                 requester_id: str = None, auth_token: str = None,
                 user_info: dict = None, correlation_id: str = None):
        self.api_id = api_id
        self.ver = ver
        self.ts = ts
        self.action = action
        self.did = did
        self.key = key
        self.msg_id = msg_id or str(uuid.uuid4())
        self.requester_id = requester_id
        self.auth_token = auth_token
        self.user_info = user_info
        self.correlation_id = correlation_id or str(uuid.uuid4())

    def to_dict(self) -> Dict:
        return {
            "apiId": self.api_id,
            "ver": self.ver,
            "ts": self.ts,
            "action": self.action,
            "did": self.did,
            "key": self.key,
            "msgId": self.msg_id,
            "requesterId": self.requester_id,
            "authToken": self.auth_token,
            "userInfo": self.user_info,
            "correlationId": self.correlation_id
        }

    def with_auth_token(self, temp_auth_token: str) -> 'RequestInfo':
        """Create a new RequestInfo instance with a temporary auth token"""
        request_info_dict = self.to_dict()
        request_info_dict["authToken"] = temp_auth_token
        return RequestInfo(
            api_id=request_info_dict["apiId"],
            ver=request_info_dict["ver"],
            ts=int(time.time() * 1000),  # Update timestamp
            action=request_info_dict["action"],
            did=request_info_dict["did"],
            key=request_info_dict["key"],
            msg_id=str(uuid.uuid4()),  # New message ID
            requester_id=request_info_dict["requesterId"],
            auth_token=temp_auth_token,
            user_info=request_info_dict["userInfo"],
            correlation_id=str(uuid.uuid4())  # New correlation ID
        )

class RequestConfig:
    _instance = None
    _default_request_info = None

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super(RequestConfig, cls).__new__(cls)
        return cls._instance

    @classmethod
    def initialize(cls, 
                  api_id: str = "DIGIT-CLIENT",
                  version: str = "1.0.0",
                  auth_token: Optional[str] = None,
                  user_info: Optional[dict] = None) -> None:
        """
        Initialize the default request configuration.
        
        Args:
            api_id (str): The API ID for the application
            version (str): API version
            auth_token (str, optional): Default authentication token
            user_info (dict, optional): User information dictionary
        """
        cls._default_request_info = RequestInfo(
            api_id=api_id,
            ver=version,
            ts=int(time.time() * 1000),
            action="",
            auth_token=auth_token,
            user_info=user_info,
            msg_id="",
            correlation_id=""
        )

    @classmethod
    def get_request_info(cls, action: str, temp_auth_token: Optional[str] = None, **kwargs) -> RequestInfo:
        """
        Get a new RequestInfo instance with default values and specified overrides.
        
        Args:
            action (str): The action being performed
            temp_auth_token (str, optional): Temporary auth token to use instead of default
            **kwargs: Additional parameters to override default values
            
        Returns:
            RequestInfo: A new RequestInfo instance
        """
        if cls._default_request_info is None:
            raise RuntimeError("RequestConfig not initialized. Call RequestConfig.initialize() first.")

        # Create new request info with current timestamp
        request_info_dict = cls._default_request_info.to_dict()
        request_info_dict.update({
            "ts": int(time.time() * 1000),
            "action": action,
            "msgId": str(uuid.uuid4()),
            "correlationId": str(uuid.uuid4()),
            **kwargs
        })

        # Override auth token if temporary one is provided
        if temp_auth_token:
            request_info_dict["authToken"] = temp_auth_token

        return RequestInfo(
            api_id=request_info_dict["apiId"],
            ver=request_info_dict["ver"],
            ts=request_info_dict["ts"],
            action=request_info_dict["action"],
            did=request_info_dict["did"],
            key=request_info_dict["key"],
            msg_id=request_info_dict["msgId"],
            requester_id=request_info_dict["requesterId"],
            auth_token=request_info_dict["authToken"],
            user_info=request_info_dict["userInfo"],
            correlation_id=request_info_dict["correlationId"]
        ) 
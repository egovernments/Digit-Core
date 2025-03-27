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

class RequestInfoBuilder:
    """Builder class for creating RequestInfo objects"""
    
    _default_config = None
    
    def __init__(self):
        # Required fields
        self._api_id: Optional[str] = None
        self._ver: Optional[str] = None
        self._action: Optional[str] = None
        
        # Optional fields with defaults
        self._ts: int = int(time.time() * 1000)
        self._did: Optional[str] = None
        self._key: Optional[str] = None
        self._msg_id: Optional[str] = None
        self._requester_id: Optional[str] = None
        self._auth_token: Optional[str] = None
        self._user_info: Optional[dict] = None
        self._correlation_id: Optional[str] = None

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
        cls._default_config = {
            "api_id": api_id,
            "ver": version,
            "auth_token": auth_token,
            "user_info": user_info
        }

    @classmethod
    def get_request_info(cls, action: str, temp_auth_token: Optional[str] = None, **kwargs) -> 'RequestInfo':
        """
        Get a new RequestInfo instance with default values and specified overrides.
        
        Args:
            action (str): The action being performed
            temp_auth_token (str, optional): Temporary auth token to use instead of default
            **kwargs: Additional parameters to override default values
            
        Returns:
            RequestInfo: A new RequestInfo instance
        """
        if cls._default_config is None:
            raise RuntimeError("RequestInfoBuilder not initialized. Call RequestInfoBuilder.initialize() first.")

        builder = cls()
        
        # Apply default configuration
        builder.with_api_id(cls._default_config["api_id"])
        builder.with_version(cls._default_config["ver"])
        if cls._default_config["auth_token"]:
            builder.with_auth_token(cls._default_config["auth_token"])
        if cls._default_config["user_info"]:
            builder.with_user_info(cls._default_config["user_info"])
            
        # Apply action and any overrides
        builder.with_action(action)
        if temp_auth_token:
            builder.with_auth_token(temp_auth_token)
            
        # Apply any additional kwargs
        for key, value in kwargs.items():
            method_name = f"with_{key}"
            if hasattr(builder, method_name):
                getattr(builder, method_name)(value)
                
        return builder.build()

    def with_api_id(self, api_id: str) -> 'RequestInfoBuilder':
        self._api_id = api_id
        return self

    def with_version(self, ver: str) -> 'RequestInfoBuilder':
        self._ver = ver
        return self

    def with_action(self, action: str) -> 'RequestInfoBuilder':
        self._action = action
        return self

    def with_timestamp(self, ts: int) -> 'RequestInfoBuilder':
        self._ts = ts
        return self

    def with_did(self, did: str) -> 'RequestInfoBuilder':
        self._did = did
        return self

    def with_key(self, key: str) -> 'RequestInfoBuilder':
        self._key = key
        return self

    def with_msg_id(self, msg_id: str) -> 'RequestInfoBuilder':
        self._msg_id = msg_id
        return self

    def with_requester_id(self, requester_id: str) -> 'RequestInfoBuilder':
        self._requester_id = requester_id
        return self

    def with_auth_token(self, auth_token: str) -> 'RequestInfoBuilder':
        self._auth_token = auth_token
        return self

    def with_user_info(self, user_info: dict) -> 'RequestInfoBuilder':
        self._user_info = user_info
        return self

    def with_correlation_id(self, correlation_id: str) -> 'RequestInfoBuilder':
        self._correlation_id = correlation_id
        return self

    def build(self) -> RequestInfo:
        """Build and validate the RequestInfo object"""
        # Validate required fields
        required_fields = {
            'api_id': self._api_id,
            'ver': self._ver,
            'action': self._action
        }

        missing_fields = [field for field, value in required_fields.items() if value is None]
        if missing_fields:
            raise ValueError(f"Missing required fields: {', '.join(missing_fields)}")

        return RequestInfo(
            api_id=self._api_id,
            ver=self._ver,
            ts=self._ts,
            action=self._action,
            did=self._did,
            key=self._key,
            msg_id=self._msg_id,
            requester_id=self._requester_id,
            auth_token=self._auth_token,
            user_info=self._user_info,
            correlation_id=self._correlation_id
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
                  user_info: Optional[dict] = None,
                  did: Optional[str] = None,
                  key: Optional[str] = None,
                  msg_id: Optional[str] = None,
                  requester_id: Optional[str] = None,
                  correlation_id: Optional[str] = None,
                  action: str = "",
                  ts: Optional[int] = None) -> None:
        """
        Initialize the default request configuration.
        
        Args:
            api_id (str): The API ID for the application
            version (str): API version
            auth_token (str, optional): Default authentication token
            user_info (dict, optional): User information dictionary
            did (str, optional): Device ID
            key (str, optional): Key for the request
            msg_id (str, optional): Message ID
            requester_id (str, optional): ID of the requester
            correlation_id (str, optional): Correlation ID for request tracking
            action (str, optional): Action being performed
        """
        cls._default_request_info = RequestInfo(
            api_id=api_id,
            ver=version,
            ts=ts,
            action=action,
            auth_token=auth_token,
            user_info=user_info,
            did=did,
            key=key,
            msg_id=msg_id,
            requester_id=requester_id,
            correlation_id=correlation_id
        )

    @classmethod
    def update_api_id(cls, api_id: str) -> None:
        """Update the API ID in the existing configuration."""
        if cls._default_request_info is None:
            raise RuntimeError("RequestConfig not initialized. Call RequestConfig.initialize() first.")
            
        request_info_dict = cls._default_request_info.to_dict()
        request_info_dict["apiId"] = api_id
        
        cls._default_request_info = RequestInfo(
            api_id=api_id,
            ver=request_info_dict["ver"],
            ts=int(time.time() * 1000),
            action=request_info_dict["action"],
            did=request_info_dict["did"],
            key=request_info_dict["key"],
            msg_id=str(uuid.uuid4()),
            requester_id=request_info_dict["requesterId"],
            auth_token=request_info_dict["authToken"],
            user_info=request_info_dict["userInfo"],
            correlation_id=str(uuid.uuid4())
        )

    @classmethod
    def update_version(cls, version: str) -> None:
        """Update the version in the existing configuration."""
        if cls._default_request_info is None:
            raise RuntimeError("RequestConfig not initialized. Call RequestConfig.initialize() first.")
            
        request_info_dict = cls._default_request_info.to_dict()
        request_info_dict["ver"] = version
        
        cls._default_request_info = RequestInfo(
            api_id=request_info_dict["apiId"],
            ver=version,
            ts=int(time.time() * 1000),
            action=request_info_dict["action"],
            did=request_info_dict["did"],
            key=request_info_dict["key"],
            msg_id=str(uuid.uuid4()),
            requester_id=request_info_dict["requesterId"],
            auth_token=request_info_dict["authToken"],
            user_info=request_info_dict["userInfo"],
            correlation_id=str(uuid.uuid4())
        )

    @classmethod
    def update_auth_token(cls, auth_token: str) -> None:
        """Update the auth token in the existing configuration."""
        if cls._default_request_info is None:
            raise RuntimeError("RequestConfig not initialized. Call RequestConfig.initialize() first.")
            
        request_info_dict = cls._default_request_info.to_dict()
        request_info_dict["authToken"] = auth_token
        
        cls._default_request_info = RequestInfo(
            api_id=request_info_dict["apiId"],
            ver=request_info_dict["ver"],
            ts=int(time.time() * 1000),
            action=request_info_dict["action"],
            did=request_info_dict["did"],
            key=request_info_dict["key"],
            msg_id=str(uuid.uuid4()),
            requester_id=request_info_dict["requesterId"],
            auth_token=auth_token,
            user_info=request_info_dict["userInfo"],
            correlation_id=str(uuid.uuid4())
        )

    @classmethod
    def update_user_info(cls, user_info: dict) -> None:
        """Update the user info in the existing configuration."""
        if cls._default_request_info is None:
            raise RuntimeError("RequestConfig not initialized. Call RequestConfig.initialize() first.")
            
        request_info_dict = cls._default_request_info.to_dict()
        request_info_dict["userInfo"] = user_info
        
        cls._default_request_info = RequestInfo(
            api_id=request_info_dict["apiId"],
            ver=request_info_dict["ver"],
            ts=int(time.time() * 1000),
            action=request_info_dict["action"],
            did=request_info_dict["did"],
            key=request_info_dict["key"],
            msg_id=str(uuid.uuid4()),
            requester_id=request_info_dict["requesterId"],
            auth_token=request_info_dict["authToken"],
            user_info=user_info,
            correlation_id=str(uuid.uuid4())
        )

    @classmethod
    def update_did(cls, did: str) -> None:
        """Update the device ID in the existing configuration."""
        if cls._default_request_info is None:
            raise RuntimeError("RequestConfig not initialized. Call RequestConfig.initialize() first.")
            
        request_info_dict = cls._default_request_info.to_dict()
        request_info_dict["did"] = did
        
        cls._default_request_info = RequestInfo(
            api_id=request_info_dict["apiId"],
            ver=request_info_dict["ver"],
            ts=int(time.time() * 1000),
            action=request_info_dict["action"],
            did=did,
            key=request_info_dict["key"],
            msg_id=str(uuid.uuid4()),
            requester_id=request_info_dict["requesterId"],
            auth_token=request_info_dict["authToken"],
            user_info=request_info_dict["userInfo"],
            correlation_id=str(uuid.uuid4())
        )

    @classmethod
    def update_key(cls, key: str) -> None:
        """Update the key in the existing configuration."""
        if cls._default_request_info is None:
            raise RuntimeError("RequestConfig not initialized. Call RequestConfig.initialize() first.")
            
        request_info_dict = cls._default_request_info.to_dict()
        request_info_dict["key"] = key
        
        cls._default_request_info = RequestInfo(
            api_id=request_info_dict["apiId"],
            ver=request_info_dict["ver"],
            ts=int(time.time() * 1000),
            action=request_info_dict["action"],
            did=request_info_dict["did"],
            key=key,
            msg_id=str(uuid.uuid4()),
            requester_id=request_info_dict["requesterId"],
            auth_token=request_info_dict["authToken"],
            user_info=request_info_dict["userInfo"],
            correlation_id=str(uuid.uuid4())
        )

    @classmethod
    def update_requester_id(cls, requester_id: str) -> None:
        """Update the requester ID in the existing configuration."""
        if cls._default_request_info is None:
            raise RuntimeError("RequestConfig not initialized. Call RequestConfig.initialize() first.")
            
        request_info_dict = cls._default_request_info.to_dict()
        request_info_dict["requesterId"] = requester_id
        
        cls._default_request_info = RequestInfo(
            api_id=request_info_dict["apiId"],
            ver=request_info_dict["ver"],
            ts=int(time.time() * 1000),
            action=request_info_dict["action"],
            did=request_info_dict["did"],
            key=request_info_dict["key"],
            msg_id=str(uuid.uuid4()),
            requester_id=requester_id,
            auth_token=request_info_dict["authToken"],
            user_info=request_info_dict["userInfo"],
            correlation_id=str(uuid.uuid4())
        )

    @classmethod
    def get_request_info(cls,temp_auth_token: Optional[str] = None, **kwargs) -> RequestInfo:
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
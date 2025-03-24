from typing import List, Optional
from dataclasses import dataclass
from copy import deepcopy
from datetime import datetime

@dataclass
class Role:
    code: str
    name: str
    tenant_id: str

    def to_dict(self) -> dict:
        return {
            "code": self.code,
            "name": self.name,
            "tenantId": self.tenant_id
        }

@dataclass
class CitizenUser:
    user_name: str
    password: str
    salutation: str
    name: str
    gender: str
    mobile_number: str
    email_id: str
    tenant_id: str
    roles: List[Role]
    alt_contact_number: Optional[str] = ""
    pan: Optional[str] = None
    aadhaar_number: Optional[str] = None
    permanent_address: Optional[str] = None
    permanent_city: Optional[str] = None
    permanent_pincode: Optional[str] = None
    correspondence_city: Optional[str] = None
    correspondence_pincode: Optional[str] = None
    correspondence_address: Optional[str] = None
    active: bool = True
    dob: Optional[str] = None
    pwd_expiry_date: Optional[str] = None
    locale: str = "en_IN"
    type: str = "CITIZEN"
    signature: Optional[str] = None
    account_locked: bool = False
    father_or_husband_name: Optional[str] = None
    blood_group: Optional[str] = None
    identification_mark: Optional[str] = None
    photo: Optional[str] = None
    otp_reference: Optional[str] = None

    def to_dict(self) -> dict:
        return {
            "userName": self.user_name,
            "password": self.password,
            "salutation": self.salutation,
            "name": self.name,
            "gender": self.gender,
            "mobileNumber": self.mobile_number,
            "emailId": self.email_id,
            "altContactNumber": self.alt_contact_number,
            "pan": self.pan,
            "aadhaarNumber": self.aadhaar_number,
            "permanentAddress": self.permanent_address,
            "permanentCity": self.permanent_city,
            "permanentPincode": self.permanent_pincode,
            "correspondenceCity": self.correspondence_city,
            "correspondencePincode": self.correspondence_pincode,
            "correspondenceAddress": self.correspondence_address,
            "active": self.active,
            "dob": self.dob,
            "pwdExpiryDate": self.pwd_expiry_date,
            "locale": self.locale,
            "type": self.type,
            "signature": self.signature,
            "accountLocked": self.account_locked,
            "fatherOrHusbandName": self.father_or_husband_name,
            "bloodGroup": self.blood_group,
            "identificationMark": self.identification_mark,
            "photo": self.photo,
            "otpReference": self.otp_reference,
            "tenantId": self.tenant_id,
            "roles": [role.to_dict() for role in self.roles]
        }

class CitizenUserBuilder:
    """Builder class for creating CitizenUser objects"""
    
    def __init__(self):
        # Required fields
        self._user_name: Optional[str] = None
        self._password: Optional[str] = None
        self._name: Optional[str] = None
        self._gender: Optional[str] = None
        self._mobile_number: Optional[str] = None
        self._tenant_id: Optional[str] = None
        self._roles: List[Role] = []

        # Optional fields with defaults
        self._active: bool = True
        self._locale: str = "en_IN"
        self._type: str = "CITIZEN"
        self._account_locked: bool = False

        # Optional fields
        self._salutation: Optional[str] = None
        self._email_id: Optional[str] = None
        self._alt_contact_number: Optional[str] = None
        self._pan: Optional[str] = None
        self._aadhaar_number: Optional[str] = None
        self._permanent_address: Optional[str] = None
        self._permanent_city: Optional[str] = None
        self._permanent_pincode: Optional[str] = None
        self._correspondence_city: Optional[str] = None
        self._correspondence_pincode: Optional[str] = None
        self._correspondence_address: Optional[str] = None
        self._dob: Optional[str] = None
        self._pwd_expiry_date: Optional[str] = None
        self._signature: Optional[str] = None
        self._father_or_husband_name: Optional[str] = None
        self._blood_group: Optional[str] = None
        self._identification_mark: Optional[str] = None
        self._photo: Optional[str] = None
        self._otp_reference: Optional[str] = None

    def with_user_name(self, user_name: str) -> 'CitizenUserBuilder':
        self._user_name = user_name
        return self

    def with_password(self, password: str) -> 'CitizenUserBuilder':
        self._password = password
        return self

    def with_name(self, name: str) -> 'CitizenUserBuilder':
        self._name = name
        return self

    def with_gender(self, gender: str) -> 'CitizenUserBuilder':
        self._gender = gender
        return self

    def with_mobile_number(self, mobile_number: str) -> 'CitizenUserBuilder':
        self._mobile_number = mobile_number
        return self

    def with_tenant_id(self, tenant_id: str) -> 'CitizenUserBuilder':
        self._tenant_id = tenant_id
        return self

    def with_role(self, role: Role) -> 'CitizenUserBuilder':
        self._roles.append(deepcopy(role))
        return self

    def with_roles(self, roles: List[Role]) -> 'CitizenUserBuilder':
        self._roles.extend(deepcopy(roles))
        return self

    def with_salutation(self, salutation: str) -> 'CitizenUserBuilder':
        self._salutation = salutation
        return self

    def with_email(self, email_id: str) -> 'CitizenUserBuilder':
        self._email_id = email_id
        return self

    def with_alt_contact_number(self, alt_contact_number: str) -> 'CitizenUserBuilder':
        self._alt_contact_number = alt_contact_number
        return self

    def with_pan(self, pan: str) -> 'CitizenUserBuilder':
        self._pan = pan
        return self

    def with_aadhaar(self, aadhaar_number: str) -> 'CitizenUserBuilder':
        self._aadhaar_number = aadhaar_number
        return self

    def with_permanent_address(self, address: str) -> 'CitizenUserBuilder':
        self._permanent_address = address
        return self

    def with_permanent_city(self, city: str) -> 'CitizenUserBuilder':
        self._permanent_city = city
        return self

    def with_permanent_pincode(self, pincode: str) -> 'CitizenUserBuilder':
        self._permanent_pincode = pincode
        return self

    def with_correspondence_city(self, city: str) -> 'CitizenUserBuilder':
        self._correspondence_city = city
        return self

    def with_correspondence_pincode(self, pincode: str) -> 'CitizenUserBuilder':
        self._correspondence_pincode = pincode
        return self

    def with_correspondence_address(self, address: str) -> 'CitizenUserBuilder':
        self._correspondence_address = address
        return self

    def with_active(self, active: bool) -> 'CitizenUserBuilder':
        self._active = active
        return self

    def with_dob(self, dob: str) -> 'CitizenUserBuilder':
        self._dob = dob
        return self

    def with_pwd_expiry_date(self, expiry_date: str) -> 'CitizenUserBuilder':
        self._pwd_expiry_date = expiry_date
        return self

    def with_locale(self, locale: str) -> 'CitizenUserBuilder':
        self._locale = locale
        return self

    def with_type(self, type: str) -> 'CitizenUserBuilder':
        self._type = type
        return self

    def with_signature(self, signature: str) -> 'CitizenUserBuilder':
        self._signature = signature
        return self

    def with_account_locked(self, locked: bool) -> 'CitizenUserBuilder':
        self._account_locked = locked
        return self

    def with_father_or_husband_name(self, name: str) -> 'CitizenUserBuilder':
        self._father_or_husband_name = name
        return self

    def with_blood_group(self, blood_group: str) -> 'CitizenUserBuilder':
        self._blood_group = blood_group
        return self

    def with_identification_mark(self, mark: str) -> 'CitizenUserBuilder':
        self._identification_mark = mark
        return self

    def with_photo(self, photo: str) -> 'CitizenUserBuilder':
        self._photo = photo
        return self

    def with_otp_reference(self, otp_reference: str) -> 'CitizenUserBuilder':
        self._otp_reference = otp_reference
        return self

    def build(self) -> CitizenUser:
        """Build and validate the CitizenUser object"""
        # Validate required fields
        required_fields = {
            'user_name': self._user_name,
            'password': self._password,
            'name': self._name,
            'gender': self._gender,
            'mobile_number': self._mobile_number,
            'tenant_id': self._tenant_id
        }

        missing_fields = [field for field, value in required_fields.items() if value is None]
        if missing_fields:
            raise ValueError(f"Missing required fields: {', '.join(missing_fields)}")

        if not self._roles:
            # Add default CITIZEN role if none provided
            self._roles.append(Role(
                code="CITIZEN",
                name="Citizen",
                tenant_id=self._tenant_id
            ))

        return CitizenUser(
            user_name=self._user_name,
            password=self._password,
            salutation=self._salutation,
            name=self._name,
            gender=self._gender,
            mobile_number=self._mobile_number,
            email_id=self._email_id,
            tenant_id=self._tenant_id,
            roles=self._roles,
            alt_contact_number=self._alt_contact_number,
            pan=self._pan,
            aadhaar_number=self._aadhaar_number,
            permanent_address=self._permanent_address,
            permanent_city=self._permanent_city,
            permanent_pincode=self._permanent_pincode,
            correspondence_city=self._correspondence_city,
            correspondence_pincode=self._correspondence_pincode,
            correspondence_address=self._correspondence_address,
            active=self._active,
            dob=self._dob,
            pwd_expiry_date=self._pwd_expiry_date,
            locale=self._locale,
            type=self._type,
            signature=self._signature,
            account_locked=self._account_locked,
            father_or_husband_name=self._father_or_husband_name,
            blood_group=self._blood_group,
            identification_mark=self._identification_mark,
            photo=self._photo,
            otp_reference=self._otp_reference
        ) 
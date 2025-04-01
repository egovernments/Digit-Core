from digit_client import RequestConfig, UserService, CitizenUserBuilder, Role, UserProfileUpdate, UserProfileUpdateBuilder, RequestInfoBuilder
from pprint import pprint

def main():
    try:
        # Initialize with default auth token
        auth_token = "ba5e5f01-dbb5-4c94-95cf-5fa52ffae078"
        userinfo = {
            "username": "priyanhugupta753@gmail.com",
            "password": "Scaler@123",
            "tenantId": "pg"
        }
        
        # First initialize with basic config
        RequestConfig.initialize(
            api_id="DIGIT-CLIENT",
            version="1.0.0"
        )

        # Get initial request info
        request_info = RequestConfig.get_request_info(action="GET")
        print("Initial request info:")
        print(request_info.to_dict())
        
        # Update with auth token
        RequestConfig.update_auth_token(auth_token)
        request_info = RequestConfig.get_request_info(action="GET")
        print("\nRequest info with auth token:")
        print(request_info.to_dict())
        
        # Update with user info
        RequestConfig.update_user_info(userinfo)
        request_info = RequestConfig.get_request_info(action="GET")
        print("\nRequest info with user info:")
        print(request_info.to_dict())
        
        # Create user service
        user_service = UserService()

        # Example 1: Create a basic citizen user with minimum required fields
        basic_citizen = (CitizenUserBuilder()
            .with_user_name("9353822214")  # Using mobile number as username for OTP-based login
            .with_password("Must@123NK")
            .with_name("mustak")
            .with_gender("MALE")
            .with_mobile_number("9353822214")
            .with_tenant_id("POM")
            .build())  # Will automatically add CITIZEN role
        
        print("\nBasic Citizen User:")
        pprint(basic_citizen.to_dict())

        # Example 2: Create a detailed citizen user with all fields
        detailed_citizen = (CitizenUserBuilder()
            .with_user_name("93538222114")
            .with_password("Must@123NK")
            .with_salutation("Ms")
            .with_name("mustak")
            .with_gender("MALE")
            .with_mobile_number("9353822214")
            .with_email("xyz@egovernments.org")
            .with_alt_contact_number("")
            .with_pan("VFGGC5624P")
            .with_aadhaar("96a70")
            .with_permanent_address("Dawakhana")
            .with_permanent_city("Kaikoo")
            .with_permanent_pincode("111111")
            .with_correspondence_city("banglore")
            .with_correspondence_pincode("123456")
            .with_correspondence_address("correAddress")
            .with_active(True)
            .with_dob("08/08/1999")
            .with_locale("en_IN")
            .with_type("CITIZEN")
            .with_signature("")
            .with_account_locked(False)
            .with_father_or_husband_name("Palash")
            .with_blood_group("O_POSITIVE")
            .with_identification_mark("Wears spects")
            .with_photo("a8a6cf1e-c84d-4a0c-b2d5-57ec8711ba25")
            .with_otp_reference("14856")
            .with_tenant_id("POM")
            .with_roles([
                Role(code="CITIZEN", name="Citizen", tenant_id="POM"),
                Role(code="EMPLOYEE", name="Employee", tenant_id="POM"),
                Role(code="ADMIN", name="Administrator", tenant_id="POM")
            ])
            .build())

        print("\nDetailed Citizen User:")
        pprint(detailed_citizen.to_dict())

        # Create citizen user
        response = user_service.create_user_no_validate(detailed_citizen)
        print("\nCreate Response:", response)

        # Example 3: Update existing user without validation
        update_user = (UserProfileUpdateBuilder()
            .with_id(338)
            .with_uuid("afc7eaf1-a25f-46c9-b16f-3f7de29009ff")
            .with_user_name("EGOvM134NmNmd")
            .with_name("gudduPoilce")
            .with_mobile_number("9353822214")
            .with_email("xyz123@egovernments.org")
            .with_locale("string")
            .with_type("EMPLOYEE")
            .with_roles([
                Role(code="EMPLOYEE", name="Employee", tenant_id="pg"),
                Role(code="GRO", name="Grievance Routing Officer", tenant_id="pg"),
                Role(code="SYSTEM", name="System user", tenant_id="pg"),
                Role(code="SUPERUSER", name="Super User", tenant_id="pg"),
                Role(code="HRMS_ADMIN", name="HRMS ADMIN", tenant_id="pg"),
                Role(code="DGRO", name="Department Grievance Routing Officer", tenant_id="pg")
            ])
            .with_active(True)
            .with_tenant_id("pg")
            .with_permanent_city("Kaikoo")
            # .with_gender("MALE")
            # .with_photo(None)
            .build())

        print("\nUpdating User:")
        pprint(update_user.to_dict())

        # Update user
        update_response = user_service.update_user_no_validate(update_user)
        print("\nUpdate Response:")
        pprint(update_response)

    except ValueError as ve:
        print(f"Validation error: {str(ve)}")
    except Exception as e:
        print(f"Error occurred: {str(e)}")

if __name__ == "__main__":
    main()
import unittest
from digit_client import RequestConfig, UserService, CitizenUserBuilder, Role
from datetime import datetime

class TestUserService(unittest.TestCase):
    def setUp(self):
        # Set up test environment before each test
        auth_token = "ba5e5f01-dbb5-4c94-95cf-5fa52ffae078"
        RequestConfig.initialize(
            api_id="DIGIT-CLIENT",
            version="1.0.0",
            auth_token=auth_token
        )
        self.user_service = UserService()

    def test_create_user_no_validate(self):
        # Arrange
        timestamp = datetime.now().strftime("%Y%m%d%H%M%S")
        test_user = (CitizenUserBuilder()
            .with_user_name(f"test_user_{timestamp}")
            .with_password("Must@123NK")
            .with_salutation("Ms")
            .with_name("mustak")
            .with_gender("MALE")
            .with_mobile_number(f"93538{timestamp[-6:]}")
            .with_email(f"xyz_{timestamp}@egovernments.org")
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

        # Act
        response = self.user_service.create_user_no_validate(test_user)

        # Assert
        self.assertIsNotNone(response)
        self.assertIn('ResponseInfo', response)
        
        # Check if there are any errors before proceeding
        if 'Errors' in response:
            self.fail(f"User creation failed: {response['Errors']}")
        
        self.assertIn('User', response)
        
        # Check response info
        self.assertEqual(response['ResponseInfo']['status'], '200')
        
        # Check user data
        user = response['User'][0]
        self.assertIsNotNone(user['id'])
        self.assertEqual(user['userName'], test_user.user_name)
        self.assertEqual(user['name'], test_user.name)
        self.assertEqual(user['gender'], test_user.gender)
        self.assertEqual(user['mobileNumber'], test_user.mobile_number)
        self.assertEqual(user['emailId'], test_user.email)
        self.assertEqual(user['tenantId'], test_user.tenant_id)
        
        # Check roles
        roles = user['roles']
        self.assertEqual(len(roles), 3)
        role_codes = {role['code'] for role in roles}
        self.assertSetEqual(role_codes, {'ADMIN', 'CITIZEN', 'EMPLOYEE'})
        
        # Check other important fields
        self.assertTrue(user['active'])
        self.assertEqual(user['bloodGroup'], "O+")
        self.assertEqual(user['dob'], "08/08/1999")
        self.assertIsNotNone(user['uuid'])
        self.assertIsNotNone(user['createdDate'])
        self.assertIsNotNone(user['lastModifiedDate'])

    def test_create_user_invalid_data(self):
        """Test that creating a user with missing required fields raises the correct error"""
        # Test with invalid data (missing required fields)
        invalid_user = CitizenUserBuilder().build()
        
        with self.assertRaisesRegex(ValueError, "Missing required fields:"):
            self.user_service.create_user_no_validate(invalid_user)

if __name__ == '__main__':
    unittest.main() 
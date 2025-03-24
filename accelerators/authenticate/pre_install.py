import sys
from getpass import getpass
import os

# Add parent directory to path to ensure auth.py can be imported
current_dir = os.path.dirname(os.path.abspath(__file__))
if current_dir not in sys.path:
    sys.path.insert(0, current_dir)

from auth import DigitAuth

def get_user_choice():
    print("\n=== DIGIT Client Authentication ===")
    print("Before using the package, you need to authenticate.")
    while True:
        choice = input("\nDo you want to (1) Register or (2) Login? Enter 1 or 2: ").strip()
        if choice in ['1', '2']:
            return choice
        print("Invalid choice. Please enter 1 for Register or 2 for Login.")

def handle_registration():
    print("\n=== Registration ===")
    email = input("Enter your email: ").strip()
    tenant_id = input("Enter your tenant ID: ").strip()
    
    try:
        print("\nRegistering user...")
        register_response = DigitAuth.register_user(email, tenant_id)
        
        # Check if registration was successful
        response_info = register_response.get('ResponseInfo', {})
        tenants = register_response.get('Tenants', [])
        
        if not response_info.get('status') == 'successful' or not tenants:
            print("\nRegistration failed:", register_response)
            return False, None
            
        # Store the tenant code for future use
        tenant_code = tenants[0].get('code')
        if not tenant_code:
            print("\nFailed to get tenant code from response")
            return False, None
        
        print("\nRegistration successful! Sending OTP...")
        
        # Send OTP
        otp_response = DigitAuth.send_otp(email, tenant_code)
        if not otp_response:
            print("\nFailed to send OTP")
            return False, None
        
        print("\nOTP has been sent to your email.")
        
        # Validate OTP
        otp = getpass("Enter the OTP sent to your email: ").strip()
        print("\nValidating OTP...")
        validate_response = DigitAuth.validate_otp(email, otp, tenant_code)
        
        access_token = validate_response.get('access_token')
        if not access_token:
            print("\nOTP validation failed")
            return False, None
            
        print("\nAuthentication successful! the access token is: ", access_token)
        return True, access_token
        
    except Exception as e:
        print(f"\nAn error occurred: {str(e)}")
        return False, None

def handle_login():
    print("\n=== Login ===")
    email = input("Enter your email: ").strip()
    tenant_id = input("Enter your tenant ID: ").strip()
    
    try:
        print("\nSending OTP...")
        # Send OTP
        otp_response = DigitAuth.send_otp(email, tenant_id)
        if not otp_response:
            print("\nFailed to send OTP")
            return False, None
        
        print("\nOTP has been sent to your email.")
        
        # Validate OTP
        otp = getpass("Enter the OTP sent to your email: ").strip()
        print("\nValidating OTP...")
        validate_response = DigitAuth.validate_otp(email, otp, tenant_id)
        
        access_token = validate_response.get('access_token')
        if not access_token:
            print("\nLogin failed")
            return False, None
            
        print("\nAuthentication successful! the access token is: ", access_token)
        return True, access_token
        
    except Exception as e:
        print(f"\nAn error occurred: {str(e)}")
        return False, None

def main():
    try:
        choice = get_user_choice()
        
        if choice == '1':
            return handle_registration()
        else:
            return handle_login()
    except KeyboardInterrupt:
        print("\n\nAuthentication cancelled by user.")
        return False, None
    except Exception as e:
        print(f"\nAn unexpected error occurred: {str(e)}")
        return False, None

if __name__ == "__main__":
    main() 
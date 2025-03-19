# __init__.py for digit_client package
import os
import sys
from pathlib import Path

# Store auth file in the package directory instead of home
PACKAGE_DIR = os.path.dirname(os.path.abspath(__file__))
AUTH_FILE = os.path.join(PACKAGE_DIR, '.digit_client_auth')

def check_auth():
    """Check if authentication has been done"""
    try:
        return os.path.exists(AUTH_FILE)
    except Exception:
        return False

def perform_auth():
    """Perform authentication"""
    try:
        # Ensure we import from the correct path
        # sys.path.insert(0, os.path.dirname(PACKAGE_DIR))
        from digit_client.pre_install import main as auth_main
        
        success, access_token = auth_main()
        if success and access_token:
            # Create auth file after successful authentication
            try:
                # with open(AUTH_FILE, 'w') as f:
                #     f.write(access_token)
                return True
            except Exception as e:
                print(f"Failed to create auth file: {e}")
                return False
        return False
    except Exception as e:
        print(f"Authentication error: {e}")
        return False

print("Initializing DIGIT Client...")
# Check authentication on import
if not check_auth():
    print("\nAuthentication required for DIGIT Client")
    if not perform_auth():
        print("Authentication failed. Package cannot be used.")
        sys.exit(1)
    print("Authentication successful!")

# Only import DigitAuth after successful authentication
from .auth import DigitAuth

__version__ = '0.1'
__all__ = ['DigitAuth']
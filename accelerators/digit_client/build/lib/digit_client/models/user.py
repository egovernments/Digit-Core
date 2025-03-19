class User:
    def __init__(self, name, mobile_number, email):
        self.name = name
        self.mobile_number = mobile_number
        self.email = email

    def to_dict(self):
        return {
            "name": self.name,
            "mobile_number": self.mobile_number,
            "email": self.email
        }
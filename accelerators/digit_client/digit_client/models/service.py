class Service:
    def __init__(self, service_id, service_name):
        self.service_id = service_id
        self.service_name = service_name

    def to_dict(self):
        return {
            "service_id": self.service_id,
            "service_name": self.service_name
        }
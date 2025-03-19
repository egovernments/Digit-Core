class ServiceDefinition:
    def __init__(self, definition_id, definition_details):
        self.definition_id = definition_id
        self.definition_details = definition_details

    def to_dict(self):
        return {
            "definition_id": self.definition_id,
            "definition_details": self.definition_details
        }
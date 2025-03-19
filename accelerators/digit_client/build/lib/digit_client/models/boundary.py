class Boundary:
    def __init__(self, boundary_id, boundary_name):
        self.boundary_id = boundary_id
        self.boundary_name = boundary_name

    def to_dict(self):
        return {
            "boundary_id": self.boundary_id,
            "boundary_name": self.boundary_name
        }
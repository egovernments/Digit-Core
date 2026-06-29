package org.digit.example;

import org.digit.services.boundary.BoundaryClient;
import org.digit.services.boundary.model.Boundary;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MyBusinessServiceExample {
    @Autowired
    private BoundaryClient boundaryClient;

    public void doSomething() {
        List<String> codes = List.of("BOUNDARY001", "BOUNDARY002");
        List<Boundary> boundaries = this.boundaryClient.searchBoundariesByCodes(codes);
    }
}


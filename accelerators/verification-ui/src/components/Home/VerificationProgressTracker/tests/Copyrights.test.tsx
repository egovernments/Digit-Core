import React from 'react';
import {render, screen} from "@testing-library/react";
import Copyrights from "../Copyrights";

describe("", () => {
    test("", () => {
        render(<Copyrights/>)
        expect(screen.getByText("2024 © MOSIP - All rights reserved.")).toBeInTheDocument()
    })
})


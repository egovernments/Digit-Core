import React from 'react';
import {render, screen} from "@testing-library/react";
import StepperContentHeader from "../StepperContentHeader";

describe("", () => {
    test("", () => {
        render(<StepperContentHeader/>)
        expect(screen.getByText("Credentials are digitally signed documents with tamper-evident QR codes. These QR codes can be easily verified using the Inji Verify app. Simply scan the QR code with your smartphone camera or use the dedicated verification tool on this page.")).toBeInTheDocument()
    })
})


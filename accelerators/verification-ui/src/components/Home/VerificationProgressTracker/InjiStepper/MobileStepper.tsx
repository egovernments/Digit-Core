import React from 'react';
import {Step, StepLabel, Stepper} from "@mui/material";
import {VerificationStep} from "../../../../types/data-types";
import {StepLabelContent} from "./styles";

function MobileStepper({steps, activeStep}: {steps: VerificationStep[], activeStep: number}) {
    return (
        <Stepper style={{maxHeight: '350px'}} activeStep={activeStep} orientation="horizontal" alternativeLabel>
            {steps.map((step, index) => (
                <Step key={step.label}>
                    <StepLabel>
                        <StepLabelContent>
                            {step.label}
                        </StepLabelContent>
                    </StepLabel>
                </Step>
            ))}
        </Stepper>
    );
}

export default MobileStepper;

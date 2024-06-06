import React from 'react';
import {Step, StepContent, StepLabel, Stepper, Typography} from "@mui/material";
import {VerificationStep} from "../../../../types/data-types";
import StepperIcon from "./StepperIcon";
import {StepperConnector} from "./StepperConnector";
import {StepContentContainer, StepContentDescription, StepLabelContent} from "./styles";

function DesktopStepper({steps, activeStep}: {steps: VerificationStep[], activeStep: number}) {
    return (
        <Stepper activeStep={activeStep} orientation="vertical" connector={<StepperConnector/>}>
            {steps.map((step, index) => (
                <Step key={step.label} expanded>
                    <StepLabel StepIconComponent={StepperIcon}>
                        <StepLabelContent>
                            {step.label}
                        </StepLabelContent>
                    </StepLabel>
                    <StepContentContainer
                        TransitionProps={{appear: true, unmountOnExit: false}}
                        hidden={false}>
                        <StepContentDescription>
                            {step.description}
                        </StepContentDescription>
                    </StepContentContainer>
                </Step>
            ))}
        </Stepper>
    );
}

export default DesktopStepper;
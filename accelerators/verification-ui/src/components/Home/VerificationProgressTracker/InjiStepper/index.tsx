import React from 'react';
import {Box, Step, StepContent, StepLabel, Stepper, Typography, useMediaQuery} from "@mui/material";
import DesktopStepper from "./DesktopStepper";
import MobileStepper from "./MobileStepper";
import {VerificationStep} from "../../../../types/data-types";
import {useActiveStepContext} from "../../../../pages/Home";

const steps: VerificationStep[] = [
    {
        label: 'Scan QR Code',
        description: 'Tap the button and display the QR code shown on your digital certificate / card',
    },
    {
        label: 'Activate your device’s camera',
        description:
            'A notification will prompt to activate your device’s camera',
    },
    {
        label: 'Verification',
        description: 'Validating and verification of your digital document / card'
    },
    {
        label: 'Result',
        description: 'Credibility result of your digital document / card'
    }
];

const InjiStepper = () => {
    const isDesktop = useMediaQuery('@media (min-width:768px)');
    const {getActiveStep} = useActiveStepContext();
    const activeStep = getActiveStep();

    return (
        <Box style={{marginTop: '30px'}}>
            {
                isDesktop
                    ? (<DesktopStepper steps={steps} activeStep={activeStep}/>)
                    : (<MobileStepper steps={steps} activeStep={activeStep}/>)
            }
        </Box>
    );
}

export default InjiStepper;

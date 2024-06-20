import React from 'react';
import {Box, Grid, Typography} from '@mui/material';
import {convertToTitleCase, getDisplayValue} from "../../../../utils/misc";
import StyledButton from "../commons/StyledButton";
import {SAMPLE_VERIFIABLE_CREDENTIAL} from "../../../../utils/samples";
import {SetActiveStepFunction} from "../../../../types/function-types";
import DescriptionOutlinedIcon from '@mui/icons-material/DescriptionOutlined';
import {VerificationSteps} from "../../../../utils/config";
import {VcDisplay, VcProperty, VcPropertyKey, VcPropertyValue, VcVerificationFailedContainer} from "./styles";

function VcDisplayCard({vc, setActiveStep}: {vc: any, setActiveStep: SetActiveStepFunction}) {
    return (
        <Box>
            <VcDisplay container>
                {
                    vc ? Object.keys(vc.credentialSubject)
                        .filter(key => key?.toLowerCase() !== "id" && key?.toLowerCase() !== "type")
                        .map(key => (
                            <VcProperty item xs={12} lg={6} key={key}>
                                <VcPropertyKey>
                                    {convertToTitleCase(key)}
                                </VcPropertyKey>
                                <VcPropertyValue>
                                    {getDisplayValue(vc.credentialSubject[key])}
                                </VcPropertyValue>
                            </VcProperty>
                        ))
                        : (
                            <VcVerificationFailedContainer>
                                <DescriptionOutlinedIcon fontSize={"inherit"} color={"inherit"}/>
                            </VcVerificationFailedContainer>
                        )
                }
            </VcDisplay>
            <Box style={{
                display: 'grid',
                placeContent: 'center'
            }}>
                <StyledButton style={{margin: "24px auto"}} onClick={() => {
                    setActiveStep(VerificationSteps.ScanQrCodePrompt)
                }}>
                    Scan Another QR Code
                </StyledButton>
            </Box>
        </Box>
    );
}

export default VcDisplayCard;

import React from 'react';
import {Box, useMediaQuery} from "@mui/material";
import {Description, Heading} from "./styles";

function StepperContentHeader(props: any) {
    const isTabletOrAbove = useMediaQuery("@media(min-width:600px)");
    return (
        <Box>
            <Heading variant='h4'>
                Verify your credentials in <span style={{color: '#FF7F00'}}>4 easy steps</span>
            </Heading>
            {
                isTabletOrAbove && (
                    <Description variant='body1'>
                        Credentials are digitally signed documents with tamper-evident QR codes. These QR codes can be easily verified using the Inji Verify app. Simply scan the QR code with your smartphone camera or use the dedicated verification tool on this page.
                    </Description>
                )
            }
        </Box>
    );
}

export default StepperContentHeader;
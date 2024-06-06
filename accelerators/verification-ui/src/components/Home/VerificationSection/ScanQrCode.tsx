import React, {useState} from 'react';
import {Box, Grid, Typography} from "@mui/material";
import scanQr from "../../../assets/scanner-ouline.svg";
import qr from "../../../assets/qr.svg";
import StyledButton from "./commons/StyledButton";
import {UploadQrCode} from "./UploadQrCode";
import {useActiveStepContext, useAlertMessages} from "../../../pages/Home";
import {SetScanResultFunction} from "../../../types/function-types";
import {QrScanResult, ScanStatus} from "../../../types/data-types";
import {AlertMessages, VerificationSteps} from "../../../utils/config";

const ScanQrCode = ({setScanResult}: {
    setScanResult: SetScanResultFunction
}) => {
    const {setActiveStep} = useActiveStepContext();
    const {setAlertInfo} = useAlertMessages();
    const [scanStatus, setScanStatus] = useState("NotScanned" as ScanStatus);

    function checkScanResult(scanResult: QrScanResult) {
        let alertInfo = !!scanResult.data ? AlertMessages.qrUploadSuccess: AlertMessages.qrNotDetected;
        setAlertInfo({
            message: alertInfo.message,
            severity: alertInfo.severity,
            open: true
        });
        setScanResult(scanResult);
    }

    return (
        <Grid container style={{padding: "78px 104px", textAlign: "center", display: "grid", placeContent: "center"}}>
            <Grid item xs={12} style={{
                font: 'normal normal 600 20px/24px Inter',
                marginBottom: "44px"
            }} order={0}>
                <Typography style={{font: 'normal normal 600 20px/24px Inter', padding: '3px 0'}}>
                    Scan QR Code or Upload an Image
                </Typography>
                <Typography style={{font: 'normal normal normal 16px/20px Inter', padding: '3px 0', color: '#717171'}}>
                    Please keep the QR code in the centre & clearly visible.
                </Typography>
            </Grid>
            <Grid item xs={12} order={1}>
                <Box
                    style={{
                        backgroundImage: `url(${scanQr})`,
                        backgroundSize: 'cover',
                        display: 'grid',
                        placeContent: 'center',
                        width: 'calc(min(45vw, 350px))',
                        height: 'calc(min(45vw, 350px))',
                        margin: '6px auto'
                    }}
                >
                    <div style={{
                        background: 'rgb(255, 127, 0, 0.1)',
                        borderRadius: '12px',
                        width: 'calc(min(42vw, 320px))',
                        height: 'calc(min(42vw, 320px))',
                        display: 'grid',
                        placeContent: 'center'
                    }}>
                        <img src={qr} style={{width: "100px"}}/>
                    </div>
                </Box>
            </Grid>
            <Grid item xs={12} order={scanStatus === "Failed" ? 2 : 3}>
                <UploadQrCode setScanResult={checkScanResult} setScanStatus={setScanStatus} displayMessage="Upload Another QR Code"/>
            </Grid>
            <Grid item xs={12} order={scanStatus === "Failed" ? 3 : 2}>
                <StyledButton
                    style={{margin: "6px 0", width: "350px", textAlign: 'center'}} fill onClick={() => setActiveStep(VerificationSteps.ActivateCamera)}>
                    Scan the QR Code
                </StyledButton>
            </Grid>
            {
                scanStatus !== "Failed" && (
                    <Grid item xs={12} style={{textAlign: 'center', display: 'grid', placeContent: 'center'}} order={4}>
                        <Typography style={{font: "normal normal normal 14px/17px Inter", color: "#8E8E8E", width: "280px"}}>
                            Allowed file formats: PNG/JPEG/JPG Min Size : 2 x 2 cm | Max Size : 10 x 10 cm
                        </Typography>
                    </Grid>
                )
            }
        </Grid>
    );
}

export default ScanQrCode;

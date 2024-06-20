import React from 'react';
import {Alert, Snackbar} from "@mui/material";
import {AlertInfo} from "../../types/data-types";

const AlertMessage = ({alertInfo, handleClose}: {
    alertInfo: AlertInfo, handleClose: () => void
}) => {
    return (
        <Snackbar
            open={alertInfo.open}
            autoHideDuration={6000}
            onClose={handleClose}
            message={alertInfo.message}
            anchorOrigin={{vertical: "top", horizontal: "right"}}
        >
            <Alert
                onClose={handleClose}
                severity={alertInfo.severity}
                variant="filled"
                sx={{ width: '100%' }}
                style={{
                    borderRadius: '10px',
                    padding: '16px 18px'
                }}
            >
                {alertInfo.message}
            </Alert>
        </Snackbar>
    );
}

export default AlertMessage;
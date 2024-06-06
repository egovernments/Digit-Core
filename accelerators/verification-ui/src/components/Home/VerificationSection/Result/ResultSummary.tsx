import React from 'react';
import {Grid, Typography} from "@mui/material";
import {ReactComponent as VerificationSuccessIcon} from "../../../../assets/verification-success-icon.svg";
import {ReactComponent as VerificationFailedIcon} from "../../../../assets/verification-failed-icon.svg";
import {SetActiveStepFunction} from "../../../../types/function-types";

const ResultSummary = ({success}: {
    success: boolean
}) => {
    return (
        <Grid container>
            <Grid item xs={12}>
                <Grid container style={{
                    display: "grid",
                    placeItems: "center",
                    placeContent: "center",
                    paddingTop: "30px"
                }}>
                    <Grid item xs={12} style={{
                        borderRadius: "50%",
                        backgroundColor: "white",
                        height: "68px",
                        width: "68px",
                        display: "grid",
                        placeContent: "center",
                        color: success ? "#4B9D1F": "#CB4242",
                        fontSize: "24px",
                        margin: "7px auto"
                    }}>
                        {success ? <VerificationSuccessIcon/> : <VerificationFailedIcon/>}
                    </Grid>
                    <Grid item xs={12}>
                        <Typography style={{
                            font: "normal normal bold 20px/24px Inter",
                            margin: "7px auto"
                        }}>
                            Results
                        </Typography>
                    </Grid>
                    <Grid item xs={12}>
                        <Typography style={{font: "normal normal normal 16px/20px Inter"}}>
                            {success
                                ? "Congratulations, the given certificate is valid!"
                                : "Unfortunately, the given certificate is Invalid!"}
                        </Typography>
                    </Grid>
                </Grid>
            </Grid>
            <Grid item xs={12}>

            </Grid>
        </Grid>
    );
}

export default ResultSummary;

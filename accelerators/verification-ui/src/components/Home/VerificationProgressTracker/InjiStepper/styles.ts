import styled from "@emotion/styled";
import {StepContent, Typography} from "@mui/material";

export const StepperIconContainer = styled('div')(({ ownerState }: {
    ownerState: { completed: boolean, active: boolean }
}) => ({
    backgroundColor: '#FFF',
    color: '#FF8F00',
    ...((ownerState.active || ownerState.completed) && {
        backgroundColor: '#FF8F00',
        color: '#FFF',
    }),
    border: '1px solid #FF8F00',
    zIndex: 1,
    width: 24,
    height: 24,
    display: 'flex',
    borderRadius: '50%',
    justifyContent: 'center',
    alignItems: 'center',
    font: 'normal normal 600 12px/15px Inter'
}));

export const StepLabelContent = styled(Typography)`
    font: normal normal bold 16px/20px Inter;
`;

export const StepContentContainer = styled(StepContent)`
    border-color: #FF7F00;
    padding-bottom: 30px;
    display: block
`

export const StepContentDescription = styled(Typography)`
    font: normal normal normal 14px/19px Inter;
    color: #535353;
`

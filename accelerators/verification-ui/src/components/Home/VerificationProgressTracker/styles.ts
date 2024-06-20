import {Box, Typography} from "@mui/material";
import styled from "@emotion/styled";

export const VerificationProgressTrackerContainer = styled(Box)`
    background: #FAFBFD 0 0 no-repeat padding-box;
    padding: 0 76px;
    margin-top: 0;
    height: 100vh;
    max-height: 100vh;
`;

export const NavbarContainer = styled(Box)`
    height: 52px;
    margin: 60px 0;
`

export const Heading = styled(Typography)`
    font: normal normal bold 26px/31px Inter;
    margin: 6px 0
`

export const Description = styled(Typography)`
    font: normal normal normal 16px/21px Inter;
    margin: 6px 0
`

export const CopyrightsContainer = styled(Box)`
    position: fixed;
    bottom: 0;
    width: 50%;
    display: grid;
    place-content: center;
    background: white;
`

export const CopyrightsContent = styled(Typography)`
    font: normal normal normal 14px/17px Inter;
    padding: 16px 0;
    color: #707070;
    width: 100%;
    text-align: center;
`
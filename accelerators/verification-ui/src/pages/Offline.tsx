import React from 'react';
import {Box} from "@mui/material";
import injiLogo from '../assets/inji-logo.svg';
import SomethingWentWrong from "../components/SomethingWentWrong";

function Offline(props: any) {
    return (
        <Box style={{
            padding: '46px 80px',
            background: '#FAFBFD 0% 0% no-repeat padding-box',
            height: '100%'
        }}>
            <img src={injiLogo}/>
            <SomethingWentWrong/>
        </Box>
    );
}

export default Offline;
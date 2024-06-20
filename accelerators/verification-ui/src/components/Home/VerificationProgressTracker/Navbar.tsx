import React from 'react';
import injiLogo from "../../../assets/inji-logo.svg";
import {NavbarContainer} from "./styles";

function Navbar(props: any) {
    // Logo goes here
    return (
        <NavbarContainer>
            <img src={injiLogo}/>
        </NavbarContainer>
    );
}

export default Navbar;

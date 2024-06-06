import React, {ReactElement} from 'react';
import {Button, ButtonProps} from "@mui/material";

type StyledButtonProps = ButtonProps & {
    fill?: boolean,
    icon?: ReactElement
}

function StyledButton(props: StyledButtonProps) {
    return (
        <Button
            {...props}
            style={{
                background: `${props.fill ? '#FF7F00' : '#FFFFFF'} 0% 0% no-repeat padding-box`,
                border: '2px solid #FF7F00',
                borderRadius: '9999px',
                opacity: 1,
                padding: '18px 28px',
                color: props.fill ? '#FFFFFF' : '#FF7F00',
                ...props.style
            }}
        >
            {
                props.icon && (
                    <span style={{display: "inline-grid", marginRight: "6px"}}>
                        {props.icon}
                    </span>
                )
            }
            <span style={{
                font: 'normal normal bold 16px/21px Inter',
                textTransform: 'none'
            }}>
                {props.children}
            </span>
        </Button>
    );
}

export default StyledButton;

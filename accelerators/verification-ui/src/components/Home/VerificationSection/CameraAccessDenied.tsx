import * as React from 'react';
import Box from '@mui/material/Box';
import Modal from '@mui/material/Modal';
import Fade from '@mui/material/Fade';
import Typography from '@mui/material/Typography';
import NoPhotographyIcon from '@mui/icons-material/NoPhotography';
import StyledButton from "./commons/StyledButton";

const style = {
    position: 'absolute',
    top: '50%',
    left: '50%',
    transform: 'translate(-50%, -50%)',
    width: 570,
    bgcolor: 'background.paper',
    boxShadow: 24,
    p: 4,
    fontSize: '52px',
    display: "grid",
    placeItems: 'center',
    borderRadius: '20px',
    outline: 'none',
    padding: '38px'
};

const CameraAccessDenied = ({open, handleClose}: {open: boolean, handleClose: () => void}) =>  {

    return (
        <Modal
            open={open}
            onClose={handleClose}
            closeAfterTransition
        >
            <Fade in={open}>
                <Box sx={style}>
                    <NoPhotographyIcon fontSize={"inherit"} style={{color: '#FF8F00', margin: '12px auto'}}/>
                    <Typography style={{font: 'normal normal 600 20px/24px Inter', margin: '12px auto'}}>
                        Camera Access Denied
                    </Typography>
                    <Typography style={{font: 'normal normal normal 16px/20px Inter', color: '#707070', margin: '12px auto'}}>
                        We need your camera to scan the code. Go to your browser settings and allow camera access for this website.
                    </Typography>
                    <StyledButton
                        onClick={handleClose}
                        style={{width: '180px', margin: '18px auto'}}>
                        I Understand
                    </StyledButton>
                </Box>
            </Fade>
        </Modal>
    );
}

export default CameraAccessDenied;

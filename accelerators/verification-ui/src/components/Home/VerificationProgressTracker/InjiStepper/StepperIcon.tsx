import {StepperIconContainer} from "./styles";

const StepperIcon = (props: any) => {
    const { active, completed, className } = props;

    return (
        <StepperIconContainer ownerState={{ completed, active }} className={className}>
            {props.icon}
        </StepperIconContainer>
    );
}

export default StepperIcon;

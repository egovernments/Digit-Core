import React from "react";
import PropTypes from "prop-types";

const Button = (props) => {
  let className = props?.variation === "primary" ? "digit-button-primary" : props?.variation ? props?.variation : "digit-button-secondary";
  return (
    <button
      ref={props?.ref}
      className={`${className} ${props?.className ? props?.className : ""} ${props?.isDisabled ? "disabled" : ""}`}
      type={props?.submit ? "submit" : props.type || "button"}
      form={props.formId}
      onClick={props.onClick}
      disabled={props?.isDisabled || null}
      style={props.style ? props.style : null}
    >
      {props?.icon && props.icon}
      <h2 style={{ ...props?.textStyles }} className="digit-button-label">
        {props.label}
      </h2>
    </button>
  );
};

Button.propTypes = {
  isDisabled: PropTypes.bool,
  /**
   * ButtonSelector content
   */
  label: PropTypes.string.isRequired,
  /**
   * button border theme
   */
  variation: PropTypes.string,
  /**
   * button icon if any
   */
  icon: PropTypes.element,
  /**
   * click handler
   */
  onClick: PropTypes.func.isRequired,
  /**
   * Custom classname
   */
  className: PropTypes.string,
  /**
   * Custom styles
   */
  style: PropTypes.object,
  /**
   * Custom label style or h2 style
   */
  textStyles: PropTypes.object,
};

Button.defaultProps = {
  label: "TEST",
  variation: "primary",
  onClick: () => {},
};

export default Button;

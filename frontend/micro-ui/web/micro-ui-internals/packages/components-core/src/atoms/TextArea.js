import React from "react";
import PropTypes from "prop-types";

const TextArea = (props) => {
  const user_type = window?.Digit?.SessionStorage.get("userType");

  return (
    <React.Fragment>
      <textarea
        placeholder={props.placeholder}
        name={props.name}
        ref={props.inputRef}
        style={props.style}
        id={props.id}
        value={props.value}
        onChange={props.onChange}
        className={`${user_type !== "citizen" ? "digit-employee-card-textarea" : "digit-card-textarea"} ${props.disable && "disabled"} ${
          props?.className ? props?.className : ""
        } ${props?.variant ? props?.variant : ""}`}
        minLength={props.minlength}
        maxLength={props.maxlength}
        autoComplete="off"
        disabled={props.disabled}
        pattern={props?.validation && props.ValidationRequired ? props?.validation?.pattern : props.pattern}
      ></textarea>
      {<p className="digit-cell-text">{props.hintText}</p>}
    </React.Fragment>
  );
};

TextArea.propTypes = {
  placeholder: PropTypes.string,
  name: PropTypes.string.isRequired,
  inputRef: PropTypes.oneOfType([PropTypes.func, PropTypes.object]),
  style: PropTypes.object,
  id: PropTypes.string,
  value: PropTypes.string,
  onChange: PropTypes.func,
  className: PropTypes.string,
  disable: PropTypes.bool,
  minlength: PropTypes.number,
  maxlength: PropTypes.number,
  autoComplete: PropTypes.string,
  disabled: PropTypes.bool,
  pattern: PropTypes.string,
  validation: PropTypes.object,
  ValidationRequired: PropTypes.bool,
  hintText: PropTypes.string,
};

TextArea.defaultProps = {
  inputRef: undefined,
  onChange: undefined,
};

export default TextArea;

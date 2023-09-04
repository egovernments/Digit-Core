import React from "react";
import PropTypes from "prop-types";
import TextInput from "./TextInput";

const MobileNumber = (props) => {
  const user_type = window?.Digit?.SessionStorage.get("userType");

  const onChange = (e) => {
    let val = e.target.value;
    if (isNaN(val) || [" ", "e", "E"].some((e) => val.includes(e)) || val.length > (props.maxLength || 10)) {
      val = val.slice(0, -1);
    }
    props?.onChange?.(val);
  };

  return (
    <React.Fragment>
      <div className={`digit-mobile-number-container ${props?.className ? props?.className : ""}`} style={props?.style}>
        {!props.hideSpan ? (
          <span style={props?.style} className="digit-citizen-card-input digit-citizen-card-input--front">
            +91
          </span>
        ) : null}
        <div
          className={`digit-text-input ${user_type === "employee" ? "" : "digit-text-mobile-input-width"} ${props.className ? props.className : ""}`}
        >
          <TextInput
            userType={props.userType}
            isMandatory={props.isMandatory}
            name={props.name}
            placeholder={props.placeholder}
            onChange={onChange}
            inputRef={props.inputRef}
            value={props.value}
            className={props.className}
            style={{ ...props.style }}
            maxLength={props.maxLength}
            minlength={props.minlength}
            max={props.max}
            pattern={props.pattern}
            min={props.min}
            disable={props.disable}
            hideSpan={props.hideSpan}
            title={props.title}
            step={props.step}
            autoFocus={props.autoFocus}
            onBlur={props.onBlur}
            variant={props?.variant}
          />
        </div>
      </div>
    </React.Fragment>
  );
};

MobileNumber.propTypes = {
  userType: PropTypes.string,
  isMandatory: PropTypes.bool,
  name: PropTypes.string,
  placeholder: PropTypes.string,
  onChange: PropTypes.func,
  inputRef: PropTypes.oneOfType([PropTypes.func, PropTypes.shape({ current: PropTypes.instanceOf(Element) })]),
  value: PropTypes.any,
  className: PropTypes.string,
  style: PropTypes.object,
  maxLength: PropTypes.number,
  minlength: PropTypes.number,
  max: PropTypes.number,
  pattern: PropTypes.string,
  min: PropTypes.number,
  disable: PropTypes.bool,
  errorStyle: PropTypes.bool,
  hideSpan: PropTypes.bool,
  title: PropTypes.string,
  step: PropTypes.string,
  autoFocus: PropTypes.bool,
  onBlur: PropTypes.func,
};

MobileNumber.defaultProps = {
  isMandatory: false,
};

export default MobileNumber;

import React, { useState, useRef } from "react";
import PropTypes from "prop-types";

const DatePicker = (props) => {
  const dateInp = useRef();
  const isCitizen = window?.Digit?.SessionStorage.get("userType") === "citizen" ? true : false;

  const selectDate = (e) => {
    const date = e.target.value;
    props?.onChange?.(date);
  };

  return (
    <div className={`digit-date-picker ${isCitizen ? "citizen" : ""} ${props?.className ? props?.className : ""}`} style={props?.style || {}}>
      <React.Fragment>
        <input
          className={`digit-employee-card-input ${props.disabled ? "disabled" : ""}`}
          style={{ ...props.style }}
          value={props.date ? props.date : ""}
          type="date"
          ref={dateInp}
          disabled={props.disabled}
          onChange={selectDate}
          defaultValue={props.defaultValue}
          min={props.min}
          max={props.max}
          required={props.isRequired || false}
          onBlur={props.onBlur}
        />
      </React.Fragment>
    </div>
  );
};

DatePicker.propTypes = {
  disabled: PropTypes.bool,
  date: PropTypes.any,
  min: PropTypes.any,
  max: PropTypes.any,
  defaultValue: PropTypes.any,
  onChange: PropTypes.func,
};

export default DatePicker;

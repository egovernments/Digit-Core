import React, { useEffect, useState } from "react";
import PropTypes from "prop-types";
import { SVG } from "./SVG";

const TextInput = (props) => {
  const { variant } = props;
  const user_type = window?.Digit?.SessionStorage.get("userType");
  const [date, setDate] = useState(props?.type === "date" && props?.value);
  const data = props?.watch
    ? {
        fromDate: props?.watch("fromDate"),
        toDate: props?.watch("toDate"),
      }
    : {};

  const handleDate = (event) => {
    const { value } = event.target;
    setDate(getDDMMYYYY(value));
  };

  const inputClassNameForMandatory = `${user_type ? "digit-employee-card-input-error" : "digit-card-input-error"} ${
    props.disable ? "disabled" : ""
  } ${props.customClass || ""}`;

  const inputClassName = `${user_type ? "digit-employee-card-input" : "digit-citizen-card-input"} ${props.disable ? "disabled" : ""} focus-visible ${
    props.errorStyle ? "digit-employee-card-input-error" : ""
  }`;

  return (
    <React.Fragment>
      <div
        className={`digit-text-input ${user_type === "employee" ? "" : "digit-text-input-width"} ${props?.className ? props?.className : ""} ${
          variant ? variant : ""
        }`}
        style={props?.textInputStyle ? { ...props.textInputStyle } : {}}
      >
        {props.isMandatory ? (
          <input
            type={props?.validation && props.ValidationRequired ? props?.validation?.type : props.type || "text"}
            name={props.name}
            id={props.id}
            className={inputClassNameForMandatory}
            placeholder={props.placeholder}
            onChange={(event) => {
              if (props?.type === "number" && props?.maxlength) {
                if (event.target.value.length > props?.maxlength) {
                  event.target.value = event.target.value.slice(0, -1);
                }
              }
              if (props?.onChange) {
                props?.onChange(event);
              }
              if (props.type === "date") {
                handleDate(event);
              }
            }}
            ref={props.inputRef}
            value={props.value}
            style={{ ...props.style }}
            defaultValue={props.defaultValue}
            minLength={props.minlength}
            maxLength={props.maxlength}
            max={props.max}
            pattern={props?.validation && props.ValidationRequired ? props?.validation?.pattern : props.pattern}
            min={props.min}
            readOnly={props.disable}
            title={props?.validation && props.ValidationRequired ? props?.validation?.title : props.title}
            step={props.step}
            autoFocus={props.autoFocus}
            onBlur={props.onBlur}
            autoComplete="off"
            disabled={props.disabled}
            onFocus={props?.onFocus}
          />
        ) : (
          <input
            type={props?.validation && props.ValidationRequired ? props?.validation?.type : props.type || "text"}
            name={props.name}
            id={props.id}
            className={inputClassName}
            placeholder={props.placeholder}
            onChange={(event) => {
              if (props?.type === "number" && props?.maxlength) {
                if (event.target.value.length > props?.maxlength) {
                  event.target.value = event.target.value.slice(0, -1);
                }
              }
              if (props?.onChange) {
                props?.onChange(event);
              }
              if (props.type === "date") {
                handleDate(event);
              }
            }}
            ref={props.inputRef}
            value={props.value}
            style={{ ...props.style }}
            defaultValue={props.defaultValue}
            minLength={props.minlength}
            maxLength={props.maxlength}
            max={props.max}
            required={
              props?.validation && props.ValidationRequired
                ? props?.validation?.isRequired
                : props.isRequired || (props.type === "date" && (props.name === "fromDate" ? data.toDate : data.fromDate))
            }
            pattern={props?.validation && props.ValidationRequired ? props?.validation?.pattern : props.pattern}
            min={props.min}
            readOnly={props.disable}
            title={props?.validation && props.ValidationRequired ? props?.validation?.title : props.title}
            step={props.step}
            autoFocus={props.autoFocus}
            onBlur={props.onBlur}
            onKeyPress={props.onKeyPress}
            autoComplete="off"
            disabled={props.disabled}
            onFocus={props?.onFocus}
          />
        )}
        {/* {props.type === "date" && <DatePicker {...props} date={date} setDate={setDate} data={data} />} */}
        {props.signature && props.signatureImg}
        {props.customIcon === "geolocation" && (
          <span className="digit-cursor-pointer" onClick={props?.onIconSelection}>
            <SVG.AddLocation className="digit-text-input-customIcon" />
          </span>
        )}
      </div>
    </React.Fragment>
  );
};

TextInput.propTypes = {
  userType: PropTypes.string,
  isMandatory: PropTypes.bool,
  name: PropTypes.string.isRequired,
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
  onKeyPress: PropTypes.func,
  textInputStyle: PropTypes.object,
  defaultValue: PropTypes.any,
  customClass: PropTypes.string,
  signature: PropTypes.bool,
  signatureImg: PropTypes.node,
  customIcon: PropTypes.string,
  onIconSelection: PropTypes.func,
  type: PropTypes.string,
  watch: PropTypes.func,
  onFocus: PropTypes.func,
};

TextInput.defaultProps = {
  isMandatory: false,
};

function DatePicker(props) {
  useEffect(() => {
    if (props?.shouldUpdate) {
      props?.setDate(getDDMMYYYY(props?.data[props.name], "yyyymmdd"));
    }
  }, [props?.data]);

  useEffect(() => {
    props.setDate(getDDMMYYYY(props?.defaultValue));
  }, []);

  return (
    <input
      type="text"
      className={`${props.disable && "disabled"} digit-card-date-input`}
      name={props.name}
      id={props.id}
      placeholder={props.placeholder}
      defaultValue={props.date}
      readOnly={true}
    />
  );
}

function getDDMMYYYY(date) {
  if (!date) return "";

  return new Date(date).toLocaleString("en-In").split(",")[0];
}

export default TextInput;

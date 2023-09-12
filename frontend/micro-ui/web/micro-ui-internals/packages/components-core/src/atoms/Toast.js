import React from "react";
import PropTypes from "prop-types";
import { SVG } from "./SVG";
import Button from "./Button";

const Toast = (props) => {
  const variant = props?.error ? "digit-error" : props?.warning ? "digit-warning" : props?.variant ? props?.variant : "";
  const isWarningButtons = props?.isWarningButtons ? "digit-warning-buttons" : "";

  if (props.error) {
    return (
      <div className={`digit-toast-success ${variant}`} style={{ ...props.style }}>
        <SVG.Error fill="#fff" />
        <h2 style={{ ...props.labelstyle }}>{props.label}</h2>
        {props.isDleteBtn ? <SVG.Delete fill="none" className="digit-toast-close-btn" onClick={props.onClose} /> : null}
      </div>
    );
  }

  if (props.warning) {
    return (
      <div>
        <div className={`digit-toast-success ${variant} ${isWarningButtons}`} style={{ ...props.style }}>
          {!props?.isWarningButtons ? (
            <div className={`digit-toast-success ${variant} ${isWarningButtons}`} style={{ ...props.style }}>
              <SVG.Error fill="#fff" />
              <h2>{props.label}</h2>
              {props.isDleteBtn ? <SVG.Delete fill="none" className="digit-toast-close-btn" onClick={props.onClose} /> : null}
            </div>
          ) : (
            <div className="digit-toast-sub-container">
              <SVG.Error fill="#fff" />
              <h2>{props.label}</h2>
              {props.isDleteBtn ? <SVG.Delete fill="none" className="digit-toast-close-btn" onClick={props.onClose} /> : null}
            </div>
          )}
          {props?.isWarningButtons ? (
            <div className="digit-warning-button-container">
              <Button theme="border" label={"NO"} onClick={props.onNo} />
              <Button label={"YES"} onClick={props.onYes} />
            </div>
          ) : null}
        </div>
      </div>
    );
  }

  return (
    <div className="digit-toast-success" style={{ ...props.style }}>
      <SVG.CheckCircle />
      <h2>{props.label}</h2>
      <SVG.Delete fill="none" className="digit-toast-close-btn" onClick={props.onClose} />
    </div>
  );
};

Toast.propTypes = {
  label: PropTypes.string,
  onClose: PropTypes.func,
  isDleteBtn: PropTypes.bool,
};

Toast.defaultProps = {
  label: "",
  onClose: undefined,
  isDleteBtn: false,
};

export default Toast;

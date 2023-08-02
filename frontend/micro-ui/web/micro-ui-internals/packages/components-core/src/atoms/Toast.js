import React from "react";
import PropTypes from "prop-types";
import Button from "./Button";
import { DeleteBtn, RoundedCheck, Error } from "@egovernments/digit-ui-svg-components";

const Toast = (props) => {
  if (props.error) {
    return (
      <div className={`digit-toast-success`} style={{ backgroundColor: "red", ...props.style }}>
        <ErrorIcon />
        <h2 style={{ ...props.labelstyle }}>{props.label}</h2>
        {props.isDleteBtn ? <DeleteBtn fill="none" className="digit-toast-close-btn" onClick={props.onClose} height="18px" width="180x" /> : null}
      </div>
    );
  }

  if (props.warning) {
    return (
      <div>
        <div
          className={`digit-toast-success`}
          style={
            props?.isWarningButtons
              ? { backgroundColor: "#EA8A3B", display: "block", ...props.style }
              : { backgroundColor: "#EA8A3B", ...props.style }
          }
        >
          {!props?.isWarningButtons ? (
            <div className={`digit-toast-success`} style={{ backgroundColor: "#EA8A3B", ...props.style }}>
              <Error />
              <h2 style={{ marginLeft: "10px" }}>{props.label}</h2>
              {props.isDleteBtn ? <DeleteBtn fill="none" className="digit-toast-close-btn" onClick={props.onClose} /> : null}
            </div>
          ) : (
            <div style={{ display: "flex" }}>
              <Error />
              <h2 style={{ marginLeft: "10px" }}>{props.label}</h2>
              {props.isDleteBtn ? <DeleteBtn fill="none" className="digit-toast-close-btn" onClick={props.onClose} /> : null}
            </div>
          )}
          {props?.isWarningButtons ? (
            <div style={{ width: "100%", display: "flex", justifyContent: "flex-end" }}>
              <Button theme="border" label={"NO"} onSubmit={props.onNo} style={{ marginLeft: "10px" }} />
              <Button label={"YES"} onSubmit={props.onYes} style={{ marginLeft: "10px" }} />
            </div>
          ) : null}
        </div>
      </div>
    );
  }

  return (
    <div className={`digit-toast-success`} style={{ ...props.style }}>
      <RoundedCheck />
      <h2>{props.label}</h2>
      {props.isDleteBtn ? <DeleteBtn fill="none" className="digit-toast-close-btn" onClick={props.onClose} /> : null}
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

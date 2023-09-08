import React from "react";
import PropTypes from "prop-types";
import { SVG } from "./SVG";
import Button from "./Button";

const Toast = (props) => {
  if (props.error) {
    return (
      <div className="toast-success" style={{ backgroundColor: "red", ...props.style }}>
        <SVG.Error />
        <h2 style={{...props.labelstyle}}>{props.label}</h2>
        { props.isDleteBtn ? <SVG.Delete fill="none" className="toast-close-btn" onClick={props.onClose} /> : null }
      </div>
    );
  }

  if (props.warning) {
    return (
      <div>
        <div className="toast-success" style={props?.isWarningButtons ? { backgroundColor: "#EA8A3B", display: "block", ...props.style } : { backgroundColor: "#EA8A3B", ...props.style }}>
          {!props?.isWarningButtons ?
            <div className="toast-success" style={{ backgroundColor: "#EA8A3B", ...props.style }}>
              <SVG.Error />
              <h2 style={{ marginLeft: "10px" }}>{props.label}</h2>
              {props.isDleteBtn ? <SVG.Delete fill="none" className="toast-close-btn" onClick={props.onClose} /> : null}
            </div> : <div style={{ display: "flex" }}>
              <SVG.Error />
              <h2 style={{ marginLeft: "10px" }}>{props.label}</h2>
              {props.isDleteBtn ? <SVG.Delete fill="none" className="toast-close-btn" onClick={props.onClose} /> : null}
            </div>
          }
          {props?.isWarningButtons ?
            <div style={{ width: "100%", display: "flex", justifyContent: "flex-end" }}>
              <Button theme="border" label={"NO"} onClick={props.onNo} style={{ marginLeft: "10px" }} />
              <Button label={"YES"} onClick={props.onYes} style={{ marginLeft: "10px" }} />
            </div> : null
          }
        </div>
      </div>
    );
  }

  return (
    <div className="toast-success" style={{ ...props.style }}>
      <SVG.CheckCircle />
      <h2>{props.label}</h2>
      <SVG.Delete fill="none" className="toast-close-btn" onClick={props.onClose} />
    </div>
  );
};

Toast.propTypes = {
  label: PropTypes.string,
  onClose: PropTypes.func,
  isDleteBtn: PropTypes.bool
};

Toast.defaultProps = {
  label: "",
  onClose: undefined,
  isDleteBtn: false
};

export default Toast;

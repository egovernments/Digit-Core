import React from "react";
import PropTypes from "prop-types";

export const CheckPoint = (props) => {
  return (
    <div className={props.isCompleted ? `digit-checkpoint-done ${props?.customClassName || ""}` : `digit-checkpoint ${props?.customClassName || ""}`} key={props.keyValue}>
      <h2></h2>
      <header>
        {props.label}
        {props.info ? <p>{props.info}</p> : null}
        {props.customChild ? props.customChild : null}
      </header>
    </div>
  );
};

export const ConnectingCheckPoints = (props) => {
  if (props.children && props.children.length >= 1) {
    return (
      <React.Fragment>
        {props.children.map((child, index) => {
          return props.children.length === ++index ? (
            <div key={index}>{child}</div>
          ) : (
            <div key={index} className="digit-checkpoint-connect-wrap">
              {child}
              <div className="digit-checkpoint-connect"></div>
            </div>
          );
        })}
      </React.Fragment>
    );
  } else {
    return null;
  }
};

CheckPoint.propTypes = {
  /**
   * Is checkpoint completed or not?
   */
  isCompleted: PropTypes.bool,
  /**
   * key value
   */
  key: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
  /**
   * checkpoint content
   */
  label: PropTypes.string,
  info: PropTypes.string,
};

CheckPoint.defaultProps = {
  isCompleted: false,
  key: 0,
  label: "",
  info: "",
};

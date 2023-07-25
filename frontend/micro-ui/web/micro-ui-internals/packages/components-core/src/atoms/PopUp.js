import React from "react";
import PropTypes from "prop-types";

const PopUp = (props) => {
  return (
    <div className={`digit-popup-wrap ${props?.className ? props?.className : ""}`} style={props?.style}>
      {props?.children}
    </div>
  );
};

PopUp.propTypes = {
  className: PropTypes.string,
  style: PropTypes.object,
  children: PropTypes.node,
};

export default PopUp;

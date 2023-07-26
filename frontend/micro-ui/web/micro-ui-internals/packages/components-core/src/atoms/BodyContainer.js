import React from "react";
import PropTypes from "prop-types";

const BodyContainer = (props) => {
  return (
    <div className={`digit-body ${props?.className ? props?.className : ""}`} style={props?.style}>
      {props.children}
    </div>
  );
};

BodyContainer.propTypes = {
  className: PropTypes.string,
  style: PropTypes.object,
  children: PropTypes.node,
};

export default BodyContainer;

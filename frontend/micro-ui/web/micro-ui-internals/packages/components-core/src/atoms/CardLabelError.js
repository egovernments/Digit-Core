import React from "react";
import PropTypes from "prop-types";

const CardLabelError = (props) => {
  return (
    <h2 className={`digit-card-label-error ${props?.className ? props?.className : ""}`} style={props.style}>
      {props.children}
    </h2>
  );
};

CardLabelError.propTypes = {
  className: PropTypes.string, // An optional string for custom class names.
  style: PropTypes.object, // An optional object for custom styles.
  children: PropTypes.node.isRequired, // Required prop for the content of the error label.
};

export default CardLabelError;

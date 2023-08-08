import React from "react";
import PropTypes from "prop-types";

const LabelFieldPair = (props) => {
  return (
    <div style={props?.style} className={`digit-label-field-pair ${props?.className ? props?.className : ""}`}>
      {props.children}
    </div>
  );
};

LabelFieldPair.propTypes = {
  className: PropTypes.string, // An optional string for custom class names.
  style: PropTypes.object, // An optional object for custom styles.
  children: PropTypes.node.isRequired, // Required prop for the content of the label-field pair.
};

export default LabelFieldPair;

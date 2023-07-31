import React from "react";
import PropTypes from "prop-types";

const ActionBar = (props) => {
  return (
    <div className={`digit-action-bar-wrap ${props?.className ? props?.className : ""}`} style={props?.style}>
      {props.children}
    </div>
  );
};

ActionBar.propTypes = {
  /** custom class of the svg icon */
  className: PropTypes.string,
  /** custom style of the svg icon */
  style: PropTypes.object,
  /** Click Event handler when icon is clicked */
  children: PropTypes.node,
};

export default ActionBar;

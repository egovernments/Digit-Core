import React from "react";

const ActionLinks = (props) => (
  <span className={`digit-action-links ${props?.className ? props?.className : ""}`} style={props?.style}>
    {props.children}
  </span>
);

export default ActionLinks;

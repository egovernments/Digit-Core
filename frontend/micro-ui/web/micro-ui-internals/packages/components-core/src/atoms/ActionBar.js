import React from "react";

const ActionBar = (props) => {
  return (
    <div className={`digit-action-bar-wrap ${props?.className ? props?.className : ""}`} style={props?.style}>
      {props.children}
    </div>
  );
};

export default ActionBar;

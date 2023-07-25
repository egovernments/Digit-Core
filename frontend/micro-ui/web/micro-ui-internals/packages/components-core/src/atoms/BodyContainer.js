import React from "react";

const BodyContainer = (props) => {
  return (
    <div className={`digit-body ${props?.className ? props?.className : ""}`} style={props?.style}>
      {props.children}
    </div>
  );
};

export default BodyContainer;

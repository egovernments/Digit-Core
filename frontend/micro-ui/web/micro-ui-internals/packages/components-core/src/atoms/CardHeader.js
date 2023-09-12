import React from "react";

const CardHeader = (props) => {
  const variant = props?.variant ? props?.variant : "";
  const className = props?.className ? props?.className : "";
  return (
    <header className={`digit-card-header ${variant} ${className}`} style={props.styles ? props.styles : {}}>
      {props.children}
    </header>
  );
};

export default CardHeader;

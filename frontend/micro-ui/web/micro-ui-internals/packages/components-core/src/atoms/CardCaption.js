import React from "react";

const CardCaption = (props) => {
  return (
    <label style={props.style} className="digit-card-caption">
      {props.children}
    </label>
  );
};

export default CardCaption;

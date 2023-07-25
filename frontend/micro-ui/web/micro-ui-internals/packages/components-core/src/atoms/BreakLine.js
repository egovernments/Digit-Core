import React from "react";

const BreakLine = ({ className = "", style = {} }) => {
  return <hr className={`digit-break-line ${className}`} style={style}></hr>;
};

export default BreakLine;

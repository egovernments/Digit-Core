import React from "react";
import PropTypes from 'prop-types';

const BreakLine = ({ className = "", style = {} }) => {
  return <hr className={`digit-break-line ${className}`} style={style}></hr>;
};

BreakLine.propTypes = {
  className: PropTypes.string,
  style: PropTypes.object,
};

export default BreakLine;

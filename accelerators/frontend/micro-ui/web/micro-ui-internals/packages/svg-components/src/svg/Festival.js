import React from "react";
import PropTypes from "prop-types";

export const Festival = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11029)">
        <path d="M13 5.7V4H16L15 2.51L16 1H11V5.7L2 12V22H9V17L12.03 15L15 17V22H22V12L13 5.7Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_1974_11029">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



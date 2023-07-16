import React from "react";
import PropTypes from "prop-types";

export const LocalBar = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11113)">
        <path d="M21 5V3H3V5L11 14V19H6V21H18V19H13V14L21 5ZM7.43 7L5.66 5H18.35L16.57 7H7.43Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_1974_11113">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



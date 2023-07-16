import React from "react";
import PropTypes from "prop-types";

export const SouthEast = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1586)">
        <path d="M19 9H17V15.59L5.41 4L4 5.41L15.59 17H9V19H19V9Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_105_1586">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



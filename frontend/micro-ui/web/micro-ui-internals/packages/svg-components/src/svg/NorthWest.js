import React from "react";
import PropTypes from "prop-types";

export const NorthWest = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1567)">
        <path d="M5 15H7V8.41L18.59 20L20 18.59L8.41 7H15V5H5V15Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_105_1567">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



import React from "react";
import PropTypes from "prop-types";

export const HourglassBottom = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_2083)">
        <path d="M18 22L17.99 16L14 12L17.99 7.99L18 2H6V8L10 12L6 15.99V22H18ZM8 7.5V4H16V7.5L12 11.5L8 7.5Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_105_2083">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



import React from "react";
import PropTypes from "prop-types";

export const CallMissed = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_2002)">
        <path d="M19.59 7L12 14.59L6.41 9H11V7H3V15H5V10.41L12 17.41L21 8.41L19.59 7Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_105_2002">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



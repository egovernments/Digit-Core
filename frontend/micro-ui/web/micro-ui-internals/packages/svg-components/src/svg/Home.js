import React from "react";
import PropTypes from "prop-types";

export const Home = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_496)">
        <path d="M10 20V14H14V20H19V12H22L12 3L2 12H5V20H10Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_105_496">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



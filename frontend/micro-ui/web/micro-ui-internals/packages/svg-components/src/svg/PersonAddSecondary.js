import React from "react";
import PropTypes from "prop-types";

export const PersonAddSecondary = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_176_829)">
        <path
          d="M13 8C13 5.79 11.21 4 9 4C6.79 4 5 5.79 5 8C5 10.21 6.79 12 9 12C11.21 12 13 10.21 13 8ZM15 10V12H18V15H20V12H23V10H20V7H18V10H15ZM1 18V20H17V18C17 15.34 11.67 14 9 14C6.33 14 1 15.34 1 18Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_176_829">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



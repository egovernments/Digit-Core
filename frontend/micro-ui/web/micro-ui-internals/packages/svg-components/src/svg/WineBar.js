import React from "react";

export const WineBar = ({ className, height = "24", width = "24", style = {}, fill = "#F47738" }) => {
  return (
    <svg width={width} height={height} className={className} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11426)">
        <path d="M6 3V9C6 11.97 8.16 14.43 11 14.91V19H8V21H16V19H13V14.91C15.84 14.43 18 11.97 18 9V3H6ZM16 8H8V5H16V8Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_1974_11426">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

import React from "react";

export const ArrowForwardIos = ({ className, height = "24", width = "24", style = {}, fill = "#F47738" }) => {
  return (
    <svg width={width} height={height} className={className} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1476)">
        <path d="M5.87988 4.12L13.7599 12L5.87988 19.88L7.99988 22L17.9999 12L7.99988 2L5.87988 4.12Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_105_1476">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

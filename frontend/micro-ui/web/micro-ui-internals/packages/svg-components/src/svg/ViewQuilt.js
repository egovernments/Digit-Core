import React from "react";

export const ViewQuilt = ({ className, height = "24", width = "24", style = {}, fill = "#F47738" }) => {
  return (
    <svg width={width} height={height} className={className} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1192)">
        <path d="M10 18H15V12H10V18ZM4 18H9V5H4V18ZM16 18H21V12H16V18ZM10 5V11H21V5H10Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_105_1192">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

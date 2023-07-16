import React from "react";

export const ViewArray = ({ className, height = "24", width = "24", style = {}, fill = "#F47738" }) => {
  return (
    <svg width={width} height={height} className={className} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1168)">
        <path d="M4 18H7V5H4V18ZM18 5V18H21V5H18ZM8 18H17V5H8V18Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_105_1168">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

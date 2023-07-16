import React from "react";

export const CallMade = ({ className, height = "24", width = "24", style = {}, fill = "#F47738" }) => {
  return (
    <svg width={width} height={height} className={className} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1996)">
        <path d="M9 5V7H15.59L4 18.59L5.41 20L17 8.41V15H19V5H9Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_105_1996">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

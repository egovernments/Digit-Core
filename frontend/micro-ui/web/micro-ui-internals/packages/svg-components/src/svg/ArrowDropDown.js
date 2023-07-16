import React from "react";

export const ArrowDropDown = ({ className, height = "24", width = "24", style = {}, fill = "#F47738" }) => {
  return (
    <svg width={width} height={height} className={className} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1464)">
        <path d="M7 10L12 15L17 10H7Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_105_1464">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

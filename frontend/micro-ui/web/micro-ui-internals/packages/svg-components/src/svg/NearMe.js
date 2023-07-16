import React from "react";

export const NearMe = ({ className, height = "24", width = "24", style = {}, fill = "#F47738" }) => {
  return (
    <svg width={width} height={height} className={className} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11268)">
        <path d="M21 3L3 10.53V11.51L9.84 14.16L12.48 21H13.46L21 3Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_1974_11268">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

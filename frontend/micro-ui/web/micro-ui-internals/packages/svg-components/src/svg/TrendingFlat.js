import React from "react";

export const TrendingFlat = ({ className, height = "24", width = "24", style = {}, fill = "#F47738" }) => {
  return (
    <svg width={width} height={height} className={className} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1128)">
        <path d="M22 12L18 8V11H3V13H18V16L22 12Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_105_1128">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

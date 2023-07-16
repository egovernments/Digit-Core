import React from "react";

export const SyncAlt = ({ className, height = "24", width = "24", style = {}, fill = "#F47738" }) => {
  return (
    <svg width={width} height={height} className={className} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1036)">
        <path d="M22 8L18 4V7H3V9H18V12L22 8Z" fill={fill} />
        <path d="M2 16L6 20V17H21V15H6V12L2 16Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_105_1036">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

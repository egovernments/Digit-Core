import React from "react";

export const ClearAll = ({ className, height = "24", width = "24", style = {}, fill = "#F47738" }) => {
  return (
    <svg width={width} height={height} className={className} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_2035)">
        <path d="M5 13H19V11H5V13ZM3 17H17V15H3V17ZM7 7V9H21V7H7Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_105_2035">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

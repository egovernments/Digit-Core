import React from "react";

export const TransitEnterExit = ({ className, height = "24", width = "24", style = {}, fill = "#F47738" }) => {
  return (
    <svg width={width} height={height} className={className} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11407)">
        <path d="M16 18H6V8H9V12.77L15.98 6L18 8.03L11.15 15H16V18Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_1974_11407">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

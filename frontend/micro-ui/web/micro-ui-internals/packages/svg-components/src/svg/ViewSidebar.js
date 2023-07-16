import React from "react";

export const ViewSidebar = ({ className, height = "24", width = "24", style = {}, fill = "#F47738" }) => {
  return (
    <svg width={width} height={height} className={className} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1195)">
        <path d="M16 20H2V4H16V20ZM18 8H22V4H18V8ZM18 20H22V16H18V20ZM18 14H22V10H18V14Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_105_1195">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

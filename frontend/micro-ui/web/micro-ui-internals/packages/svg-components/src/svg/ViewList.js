import React from "react";

export const ViewList = ({ className, height = "24", width = "24", style = {}, fill = "#F47738" }) => {
  return (
    <svg width={width} height={height} className={className} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1186)">
        <path d="M4 14H8V10H4V14ZM4 19H8V15H4V19ZM4 9H8V5H4V9ZM9 14H21V10H9V14ZM9 19H21V15H9V19ZM9 5V9H21V5H9Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_105_1186">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

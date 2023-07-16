import React from "react";

export const VerticalSplit = ({ className, height = "24", width = "24", style = {}, fill = "#F47738" }) => {
  return (
    <svg width={width} height={height} className={className} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1162)">
        <path d="M3 15H11V13H3V15ZM3 19H11V17H3V19ZM3 11H11V9H3V11ZM3 5V7H11V5H3ZM13 5H21V19H13V5Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_105_1162">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

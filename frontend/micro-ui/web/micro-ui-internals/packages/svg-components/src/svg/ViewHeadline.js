import React from "react";

export const ViewHeadline = ({ className, height = "24", width = "24", style = {}, fill = "#F47738" }) => {
  return (
    <svg width={width} height={height} className={className} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1180)">
        <path d="M4 15H20V13H4V15ZM4 19H20V17H4V19ZM4 11H20V9H4V11ZM4 5V7H20V5H4Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_105_1180">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

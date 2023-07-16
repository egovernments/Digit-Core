import React from "react";

export const Upgrade = ({ className, height = "24", width = "24", style = {}, fill = "#F47738" }) => {
  return (
    <svg width={width} height={height} className={className} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1150)">
        <path d="M16 18V20H8V18H16ZM11 7.99V16H13V7.99H16L12 4L8 7.99H11Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_105_1150">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

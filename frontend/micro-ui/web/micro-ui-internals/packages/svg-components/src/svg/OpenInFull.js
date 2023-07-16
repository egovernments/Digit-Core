import React from "react";

export const OpenInFull = ({ className, height = "24", width = "24", style = {}, fill = "#F47738" }) => {
  return (
    <svg width={width} height={height} className={className} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_684)">
        <path d="M21 11V3H13L16.29 6.29L6.29 16.29L3 13V21H11L7.71 17.71L17.71 7.71L21 11Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_105_684">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

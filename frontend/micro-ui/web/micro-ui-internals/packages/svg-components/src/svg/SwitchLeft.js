import React from "react";

export const SwitchLeft = ({ className, height = "24", width = "24", style = {}, fill = "#F47738" }) => {
  return (
    <svg width={width} height={height} className={className} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1598)">
        <path d="M8.5 8.62V15.38L5.12 12L8.5 8.62ZM10 5L3 12L10 19V5ZM14 5V19L21 12L14 5Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_105_1598">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

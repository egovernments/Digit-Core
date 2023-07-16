import React from "react";

export const SwitchRight = ({ className, height = "24", width = "24", style = {}, fill = "#F47738" }) => {
  return (
    <svg width={width} height={height} className={className} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1601)">
        <path d="M15.5 15.38V8.62L18.88 12L15.5 15.38ZM14 19L21 12L14 5V19ZM10 19V5L3 12L10 19Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_105_1601">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

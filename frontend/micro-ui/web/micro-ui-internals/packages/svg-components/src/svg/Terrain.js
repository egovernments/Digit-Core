import React from "react";

export const Terrain = ({ className, height = "24", width = "24", style = {}, fill = "#F47738" }) => {
  return (
    <svg width={width} height={height} className={className} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11385)">
        <path d="M14 6L10.25 11L13.1 14.8L11.5 16C9.81 13.75 7 10 7 10L1 18H23L14 6Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_1974_11385">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

import React from "react";

export const AddRoad = ({ className, height = "24", width = "24", style = {}, fill = "#F47738" }) => {
  return (
    <svg width={width} height={height} className={className} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_10801)">
        <path d="M20 18V15H18V18H15V20H18V23H20V20H23V18H20Z" fill={fill} />
        <path d="M20 4H18V13H20V4Z" fill={fill} />
        <path d="M6 4H4V20H6V4Z" fill={fill} />
        <path d="M13 4H11V8H13V4Z" fill={fill} />
        <path d="M13 10H11V14H13V10Z" fill={fill} />
        <path d="M13 16H11V20H13V16Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_1974_10801">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

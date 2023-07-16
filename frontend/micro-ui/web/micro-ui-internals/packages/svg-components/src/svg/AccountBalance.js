import React from "react";

export const AccountBalance = ({ className, height = "24", width = "24", style = {}, fill = "#F47738" }) => {
  return (
    <svg width={width} height={height} className={className} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_20)">
        <path d="M7 10H4V17H7V10Z" fill={fill} />
        <path d="M13.5 10H10.5V17H13.5V10Z" fill={fill} />
        <path d="M22 19H2V22H22V19Z" fill={fill} />
        <path d="M20 10H17V17H20V10Z" fill={fill} />
        <path d="M12 1L2 6V8H22V6L12 1Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_105_20">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

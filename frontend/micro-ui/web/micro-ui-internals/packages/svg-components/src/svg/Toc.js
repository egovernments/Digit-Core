import React from "react";

export const Toc = ({ className, height = "24", width = "24", style = {}, fill = "#F47738" }) => {
  return (
    <svg width={width} height={height} className={className} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1099)">
        <path d="M3 9H17V7H3V9ZM3 13H17V11H3V13ZM3 17H17V15H3V17ZM19 17H21V15H19V17ZM19 7V9H21V7H19ZM19 13H21V11H19V13Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_105_1099">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

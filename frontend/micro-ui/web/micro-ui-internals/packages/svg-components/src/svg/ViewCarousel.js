import React from "react";

export const ViewCarousel = ({ className, height = "24", width = "24", style = {}, fill = "#F47738" }) => {
  return (
    <svg width={width} height={height} className={className} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1171)">
        <path d="M7 19H17V4H7V19ZM2 17H6V6H2V17ZM18 6V17H22V6H18Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_105_1171">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

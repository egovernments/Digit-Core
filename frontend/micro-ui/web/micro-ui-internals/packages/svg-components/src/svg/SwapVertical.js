import React from "react";

export const SwapVertical = ({ className, height = "24", width = "24", style = {}, fill = "#F47738" }) => {
  return (
    <svg width={width} height={height} className={className} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1027)">
        <path d="M16 17.01V10H14V17.01H11L15 21L19 17.01H16V17.01ZM9 3L5 6.99H8V14H10V6.99H13L9 3Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_105_1027">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

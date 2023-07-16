import React from "react";

export const TakeOutDining = ({ className, height = "24", width = "24", style = {}, fill = "#F47738" }) => {
  return (
    <svg width={width} height={height} className={className} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11377)">
        <path
          fill-rule="evenodd"
          clip-rule="evenodd"
          d="M5.26 11H18.74L18.07 20H5.93L5.26 11ZM9.02 4H14.97L19 7.38L20.59 5.79L22 7.21L19.21 10H4.79L2 7.21L3.41 5.8L5 7.38L9.02 4Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_11377">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

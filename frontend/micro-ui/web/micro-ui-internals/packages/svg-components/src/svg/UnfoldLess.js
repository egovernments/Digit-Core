import React from "react";

export const UnfoldLess = ({ className, height = "24", width = "24", style = {}, fill = "#F47738" }) => {
  return (
    <svg width={width} height={height} className={className} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1604)">
        <path
          d="M7.41016 18.59L8.83016 20L12.0002 16.83L15.1702 20L16.5802 18.59L12.0002 14L7.41016 18.59ZM16.5902 5.41L15.1702 4L12.0002 7.17L8.83016 4L7.41016 5.41L12.0002 10L16.5902 5.41Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_1604">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

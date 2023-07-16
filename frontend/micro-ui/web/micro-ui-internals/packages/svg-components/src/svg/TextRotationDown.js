import React from "react";

export const TextRotationDown = ({ className, height = "24", width = "24", style = {}, fill = "#F47738" }) => {
  return (
    <svg width={width} height={height} className={className} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1068)">
        <path
          d="M21 12V10.5L10 5.75V7.85L12.2 8.75V13.75L10 14.65V16.75L21 12ZM14 9.38L19.02 11.25L14 13.12V9.38V9.38ZM6 19.75L9 16.75H7V4.25H5V16.75H3L6 19.75Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_1068">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

import React from "react";

export const UnfoldMore = ({ className, height = "24", width = "24", style = {}, fill = "#F47738" }) => {
  return (
    <svg width={width} height={height} className={className} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1607)">
        <path
          d="M12.0002 5.83L15.1702 9L16.5802 7.59L12.0002 3L7.41016 7.59L8.83016 9L12.0002 5.83ZM12.0002 18.17L8.83016 15L7.42016 16.41L12.0002 21L16.5902 16.41L15.1702 15L12.0002 18.17Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_1607">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

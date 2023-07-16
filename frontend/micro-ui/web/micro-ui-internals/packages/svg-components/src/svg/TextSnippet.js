import React from "react";

export const TextSnippet = ({ className, height = "24", width = "24", style = {}, fill = "#F47738" }) => {
  return (
    <svg width={width} height={height} className={className} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_2479)">
        <path
          d="M20.41 8.41L15.58 3.58C15.21 3.21 14.7 3 14.17 3H5C3.9 3 3 3.9 3 5V19C3 20.1 3.9 21 5 21H19C20.1 21 21 20.1 21 19V9.83C21 9.3 20.79 8.79 20.41 8.41ZM7 7H14V9H7V7ZM17 17H7V15H17V17ZM17 13H7V11H17V13Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_2479">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

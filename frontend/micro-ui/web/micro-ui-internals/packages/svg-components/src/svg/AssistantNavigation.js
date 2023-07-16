import React from "react";

export const AssistantNavigation = ({ className, height = "24", width = "24", style = {}, fill = "#F47738" }) => {
  return (
    <svg width={width} height={height} className={className} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1491)">
        <path
          d="M12 1C5.93 1 1 5.93 1 12C1 18.07 5.93 23 12 23C18.07 23 23 18.07 23 12C23 5.93 18.07 1 12 1ZM15.57 17L12 15.42L8.43 17L8.06 16.63L12 7L15.95 16.63L15.57 17V17Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_1491">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

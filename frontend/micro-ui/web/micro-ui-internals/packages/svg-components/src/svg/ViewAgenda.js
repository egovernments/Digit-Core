import React from "react";

export const ViewAgenda = ({ className, height = "24", width = "24", style = {}, fill = "#F47738" }) => {
  return (
    <svg width={width} height={height} className={className} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1165)">
        <path
          d="M20 13H3C2.45 13 2 13.45 2 14V20C2 20.55 2.45 21 3 21H20C20.55 21 21 20.55 21 20V14C21 13.45 20.55 13 20 13ZM20 3H3C2.45 3 2 3.45 2 4V10C2 10.55 2.45 11 3 11H20C20.55 11 21 10.55 21 10V4C21 3.45 20.55 3 20 3Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_1165">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

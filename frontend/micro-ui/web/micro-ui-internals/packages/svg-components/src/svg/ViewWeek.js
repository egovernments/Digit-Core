import React from "react";

export const ViewWeek = ({ className, height = "24", width = "24", style = {}, fill = "#F47738" }) => {
  return (
    <svg width={width} height={height} className={className} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1202)">
        <path
          d="M6 5H3C2.45 5 2 5.45 2 6V18C2 18.55 2.45 19 3 19H6C6.55 19 7 18.55 7 18V6C7 5.45 6.55 5 6 5ZM20 5H17C16.45 5 16 5.45 16 6V18C16 18.55 16.45 19 17 19H20C20.55 19 21 18.55 21 18V6C21 5.45 20.55 5 20 5ZM13 5H10C9.45 5 9 5.45 9 6V18C9 18.55 9.45 19 10 19H13C13.55 19 14 18.55 14 18V6C14 5.45 13.55 5 13 5Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_1202">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

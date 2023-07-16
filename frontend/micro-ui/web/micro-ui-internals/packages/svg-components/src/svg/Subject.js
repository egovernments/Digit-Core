import React from "react";
import PropTypes from "prop-types";

export const Subject = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1000)">
        <path d="M14 17H4V19H14V17ZM20 9H4V11H20V9ZM4 15H20V13H4V15ZM4 5V7H20V5H4Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_105_1000">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



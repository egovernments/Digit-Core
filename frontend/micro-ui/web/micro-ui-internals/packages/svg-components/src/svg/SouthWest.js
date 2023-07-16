import React from "react";
import PropTypes from "prop-types";

export const SouthWest = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1589)">
        <path d="M15 19V17H8.41L20 5.41L18.59 4L7 15.59V9H5V19H15Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_105_1589">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



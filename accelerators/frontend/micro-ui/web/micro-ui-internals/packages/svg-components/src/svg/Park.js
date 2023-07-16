import React from "react";
import PropTypes from "prop-types";

export const Park = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11291)">
        <path d="M16.9996 12H18.9996L11.9996 2L5.04961 12H6.99961L3.09961 18H10.0196V22H13.9796V18H20.9996L16.9996 12Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_1974_11291">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



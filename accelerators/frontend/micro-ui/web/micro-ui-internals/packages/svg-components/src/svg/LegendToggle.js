import React from "react";
import PropTypes from "prop-types";

export const LegendToggle = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1545)">
        <path d="M20 15H4V13H20V15ZM20 17H4V19H20V17ZM15 11L20 7.45V5L15 8.55L10 5L4 8.66V11L9.92 7.39L15 11Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_105_1545">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



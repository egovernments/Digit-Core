import React from "react";
import PropTypes from "prop-types";

export const PermScanWifi = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_752)">
        <path d="M12 3C6.95 3 3.15 4.85 0 7.23L12 22L24 7.25C20.85 4.87 17.05 3 12 3ZM13 16H11V10H13V16ZM11 8V6H13V8H11Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_105_752">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



PermScanWifi.propTypes = {
  /** custom width of the svg icon */
  width: PropTypes.string,
  /** custom height of the svg icon */
  height: PropTypes.string,
  /** custom colour of the svg icon */
  fill: PropTypes.string,
  /** custom class of the svg icon */
  className: PropTypes.string,
  /** custom style of the svg icon */
  style: PropTypes.object,
  /** Click Event handler when icon is clicked */
  onClick: PropTypes.func,
};

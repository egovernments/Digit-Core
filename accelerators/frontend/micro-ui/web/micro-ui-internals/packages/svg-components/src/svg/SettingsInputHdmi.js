import React from "react";
import PropTypes from "prop-types";

export const SettingsInputHdmi = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_933)">
        <path d="M18 7V4C18 2.9 17.1 2 16 2H8C6.9 2 6 2.9 6 4V7H5V13L8 19V22H16V19L19 13V7H18ZM8 4H16V7H14V5H13V7H11V5H10V7H8V4Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_105_933">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



SettingsInputHdmi.propTypes = {
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

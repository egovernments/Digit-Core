import React from "react";
import PropTypes from "prop-types";

export const SettingsCell = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_918)">
        <path
          d="M7 24H9V22H7V24ZM11 24H13V22H11V24ZM15 24H17V22H15V24ZM16 0.01L8 0C6.9 0 6 0.9 6 2V18C6 19.1 6.9 20 8 20H16C17.1 20 18 19.1 18 18V2C18 0.9 17.1 0.01 16 0.01V0.01ZM16 16H8V4H16V16Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_918">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



SettingsCell.propTypes = {
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

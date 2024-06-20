import React from "react";
import PropTypes from "prop-types";

export const SettingsBluetooth = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_912)">
        <path
          d="M11 24H13V22H11V24ZM7 24H9V22H7V24ZM15 24H17V22H15V24ZM17.71 5.71L12 0H11V7.59L6.41 3L5 4.41L10.59 10L5 15.59L6.41 17L11 12.41V20H12L17.71 14.29L13.41 10L17.71 5.71V5.71ZM13 3.83L14.88 5.71L13 7.59V3.83ZM14.88 14.29L13 16.17V12.41L14.88 14.29V14.29Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_912">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



SettingsBluetooth.propTypes = {
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

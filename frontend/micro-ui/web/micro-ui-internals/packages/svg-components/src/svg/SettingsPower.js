import React from "react";
import PropTypes from "prop-types";

export const SettingsPower = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_945)">
        <path
          d="M7 24H9V22H7V24ZM11 24H13V22H11V24ZM13 2H11V12H13V2ZM16.56 4.44L15.11 5.89C16.84 6.94 18 8.83 18 11C18 14.31 15.31 17 12 17C8.69 17 6 14.31 6 11C6 8.83 7.16 6.94 8.88 5.88L7.44 4.44C5.36 5.88 4 8.28 4 11C4 15.42 7.58 19 12 19C16.42 19 20 15.42 20 11C20 8.28 18.64 5.88 16.56 4.44V4.44ZM15 24H17V22H15V24Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_945">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



SettingsPower.propTypes = {
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

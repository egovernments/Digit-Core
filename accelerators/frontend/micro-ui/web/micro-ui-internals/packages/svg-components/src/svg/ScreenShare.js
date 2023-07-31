import React from "react";
import PropTypes from "prop-types";

export const ScreenShare = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_2246)">
        <path
          d="M20 18C21.1 18 21.99 17.1 21.99 16L22 6C22 4.89 21.1 4 20 4H4C2.89 4 2 4.89 2 6V16C2 17.1 2.89 18 4 18H0V20H24V18H20ZM13 14.47V12.28C10.22 12.28 8.39 13.13 7 15C7.56 12.33 9.11 9.67 13 9.13V7L17 10.73L13 14.47Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_2246">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



ScreenShare.propTypes = {
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

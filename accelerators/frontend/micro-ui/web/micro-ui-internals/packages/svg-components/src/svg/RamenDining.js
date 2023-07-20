import React from "react";
import PropTypes from "prop-types";

export const RamenDining = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11339)">
        <path
          d="M9 6H8V4.65L9 4.53V6ZM9 12H8V7H9V12ZM6 7H7V12H6V7ZM6 4.88L7 4.76V6H6V4.88ZM22 3V2L5 4V12H2C2 15.69 4.47 18.86 8 20.25V22H16V20.25C19.53 18.86 22 15.69 22 12H10V7H22V6H10V4.41L22 3Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_11339">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



RamenDining.propTypes = {
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

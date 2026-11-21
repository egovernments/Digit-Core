import React from "react";
import PropTypes from "prop-types";

export const BreakfastDining = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_10854)">
        <path
          fill-rule="evenodd"
          clip-rule="evenodd"
          d="M18 3H6C3.79 3 2 4.79 2 7C2 8.48 2.81 9.75 4 10.45V19C4 20.1 4.9 21 6 21H18C19.1 21 20 20.1 20 19V10.45C21.19 9.76 22 8.48 22 7C22 4.79 20.21 3 18 3ZM14 15H10V11H14V15Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_10854">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



BreakfastDining.propTypes = {
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

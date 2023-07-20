import React from "react";
import PropTypes from "prop-types";

export const DinnerDining = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_10921)">
        <path
          d="M2 19H22L20 21H4L2 19ZM5 6H6V7H5V6ZM5 4H6V5H5V4ZM9 4V5H7V4H9ZM9 7H7V6H9V7ZM6 15.23C5.64 15.34 5.31 15.51 5 15.7V8H6V15.23ZM4 16.52C3.62 16.96 3.32 17.45 3.16 18H19.98C19.99 17.84 20.01 17.67 20.01 17.5C20.01 14.46 17.55 12 14.51 12C12.22 12 10.26 13.4 9.43 15.4C8.84 15.15 8.19 15 7.5 15C7.33 15 7.17 15.02 7 15.04V8H9C10.03 8.06 10.9 7.04 11 6H21V5H11C10.9 3.95 10.03 3.03 9 3H3V4H4V5H3V6H4V7H3V8H4V16.52Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_10921">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



DinnerDining.propTypes = {
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

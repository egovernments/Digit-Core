import React from "react";
import PropTypes from "prop-types";

export const SingleBed = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_176_926)">
        <path
          d="M20 12C20 10.9 19.1 10 18 10V7C18 5.9 17.1 5 16 5H8C6.9 5 6 5.9 6 7V10C4.9 10 4 10.9 4 12V17H5.33L6 19H7L7.67 17H16.34L17 19H18L18.67 17H20V12ZM16 10H13V7H16V10ZM8 7H11V10H8V7ZM6 12H18V15H6V12Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_176_926">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



SingleBed.propTypes = {
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

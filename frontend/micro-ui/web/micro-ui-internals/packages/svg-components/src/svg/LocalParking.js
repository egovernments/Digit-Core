import React from "react";
import PropTypes from "prop-types";

export const LocalParking = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11166)">
        <path
          d="M13 3H6V21H10V15H13C16.31 15 19 12.31 19 9C19 5.69 16.31 3 13 3ZM13.2 11H10V7H13.2C14.3 7 15.2 7.9 15.2 9C15.2 10.1 14.3 11 13.2 11Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_11166">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



LocalParking.propTypes = {
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

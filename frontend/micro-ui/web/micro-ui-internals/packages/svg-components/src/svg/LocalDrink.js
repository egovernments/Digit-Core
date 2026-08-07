import React from "react";
import PropTypes from "prop-types";

export const LocalDrink = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11128)">
        <path
          d="M3 2L5.01 20.23C5.13 21.23 5.97 22 7 22H17C18.03 22 18.87 21.23 18.99 20.23L21 2H3ZM12 19C10.34 19 9 17.66 9 16C9 14 12 10.6 12 10.6C12 10.6 15 14 15 16C15 17.66 13.66 19 12 19ZM18.33 8H5.67L5.23 4H18.76L18.33 8Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_11128">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



LocalDrink.propTypes = {
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

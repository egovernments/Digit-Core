import React from "react";
import PropTypes from "prop-types";

export const Icecream = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11083)">
        <path
          fill-rule="evenodd"
          clip-rule="evenodd"
          d="M8.79 12.4L12.05 18.62L15.22 12.41C15.11 12.33 15.01 12.25 14.92 12.16C14.08 12.69 13.07 13 12 13C10.93 13 9.92 12.69 9.08 12.16C8.99 12.25 8.89 12.33 8.79 12.4ZM6.83 12.99C5.25 12.9 4 11.6 4 10C4 8.51 5.09 7.27 6.52 7.04C6.75 4.22 9.12 2 12 2C14.88 2 17.25 4.22 17.48 7.04C18.91 7.27 20 8.51 20 10C20 11.59 18.76 12.9 17.19 12.99L12.07 23L6.83 12.99Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_11083">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



Icecream.propTypes = {
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

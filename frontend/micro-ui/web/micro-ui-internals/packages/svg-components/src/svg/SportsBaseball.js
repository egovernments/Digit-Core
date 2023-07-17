import React from "react";
import PropTypes from "prop-types";

export const SportsBaseball = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_176_940)">
        <path
          d="M3.81 6.28003C2.67 7.90003 2 9.87003 2 12C2 14.13 2.67 16.1 3.81 17.72C6.23 16.95 8 14.68 8 12C8 9.32003 6.23 7.05003 3.81 6.28003Z"
          fill={fill}
        />
        <path
          d="M20.19 6.28003C17.77 7.05003 16 9.32003 16 12C16 14.68 17.77 16.95 20.19 17.72C21.33 16.1 22 14.13 22 12C22 9.87003 21.33 7.90003 20.19 6.28003Z"
          fill={fill}
        />
        <path
          d="M14 12C14 8.72 15.97 5.91 18.79 4.67C17.01 3.02 14.63 2 12 2C9.36996 2 6.98996 3.02 5.20996 4.67C8.02996 5.91 9.99996 8.72 9.99996 12C9.99996 15.28 8.02996 18.09 5.20996 19.33C6.98996 20.98 9.36996 22 12 22C14.63 22 17.01 20.98 18.79 19.33C15.97 18.09 14 15.28 14 12Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_176_940">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



SportsBaseball.propTypes = {
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

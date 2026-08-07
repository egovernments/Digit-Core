import React from "react";
import PropTypes from "prop-types";

export const PartyMode = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_176_796)">
        <path
          d="M20 4H16.83L15 2H9L7.17 4H4C2.9 4 2 4.9 2 6V18C2 19.1 2.9 20 4 20H20C21.1 20 22 19.1 22 18V6C22 4.9 21.1 4 20 4ZM12 7C13.63 7 15.06 7.79 15.98 9H12C10.34 9 9 10.34 9 12C9 12.35 9.07 12.69 9.18 13H7.1C7.04 12.68 7 12.34 7 12C7 9.24 9.24 7 12 7ZM12 17C10.37 17 8.94 16.21 8.02 15H12C13.66 15 15 13.66 15 12C15 11.65 14.93 11.31 14.82 11H16.9C16.97 11.32 17 11.66 17 12C17 14.76 14.76 17 12 17Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_176_796">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



PartyMode.propTypes = {
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

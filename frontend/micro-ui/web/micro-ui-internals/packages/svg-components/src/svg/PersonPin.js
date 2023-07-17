import React from "react";
import PropTypes from "prop-types";

export const PersonPin = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11301)">
        <path
          d="M12 2C7.03 2 3 6.03 3 11C3 15.17 5.84 18.67 9.69 19.69L12 22L14.31 19.69C18.16 18.67 21 15.17 21 11C21 6.03 16.97 2 12 2ZM12 4C13.66 4 15 5.34 15 7C15 8.66 13.66 10 12 10C10.34 10 9 8.66 9 7C9 5.34 10.34 4 12 4ZM12 18.3C9.5 18.3 7.29 17.02 6 15.08C6.03 13.09 10 12 12 12C13.99 12 17.97 13.09 18 15.08C16.71 17.02 14.5 18.3 12 18.3Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_11301">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



PersonPin.propTypes = {
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

import React from "react";
import PropTypes from "prop-types";

export const ModelTraining = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_641)">
        <path
          d="M15.5 13.5C15.5 15.5 13 17 13 18.5H11C11 17 8.5 15.5 8.5 13.5C8.5 11.57 10.07 10 12 10C13.93 10 15.5 11.57 15.5 13.5ZM13 19.5H11V21H13V19.5ZM19 13C19 14.68 18.41 16.21 17.42 17.42L18.84 18.84C20.18 17.27 21 15.23 21 13C21 10.26 19.77 7.81 17.84 6.16L16.42 7.58C17.99 8.86 19 10.82 19 13ZM16 5L12 1V4C7.03 4 3 8.03 3 13C3 15.23 3.82 17.27 5.16 18.84L6.58 17.42C5.59 16.21 5 14.68 5 13C5 9.14 8.14 6 12 6V9L16 5Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_641">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



ModelTraining.propTypes = {
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

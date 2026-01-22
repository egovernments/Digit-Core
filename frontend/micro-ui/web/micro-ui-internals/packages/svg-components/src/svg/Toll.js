import React from "react";
import PropTypes from "prop-types";

export const Toll = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1105)">
        <path
          d="M15 4C10.58 4 7 7.58 7 12C7 16.42 10.58 20 15 20C19.42 20 23 16.42 23 12C23 7.58 19.42 4 15 4ZM15 18C11.69 18 9 15.31 9 12C9 8.69 11.69 6 15 6C18.31 6 21 8.69 21 12C21 15.31 18.31 18 15 18Z"
          fill={fill}
        />
        <path
          d="M3 11.9998C3 9.38977 4.67 7.16977 7 6.34977V4.25977C3.55 5.14977 1 8.26977 1 11.9998C1 15.7298 3.55 18.8498 7 19.7398V17.6498C4.67 16.8298 3 14.6098 3 11.9998V11.9998Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_1105">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

Toll.propTypes = {
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

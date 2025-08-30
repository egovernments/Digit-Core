import React from "react";
import PropTypes from "prop-types";

export const AttachEmail = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_2402)">
        <path
          d="M21 10V4C21 2.9 20.1 2 19 2H3C1.9 2 1.01 2.9 1.01 4L1 16C1 17.1 1.9 18 3 18H14V13C14 11.34 15.34 10 17 10H21ZM11 11L3 6V4L11 9L19 4V6L11 11Z"
          fill={fill}
        />
        <path
          d="M21 14V18C21 19.1 20.1 20 19 20C17.9 20 17 19.1 17 18V13.5C17 13.22 17.22 13 17.5 13C17.78 13 18 13.22 18 13.5V18H20V13.5C20 12.12 18.88 11 17.5 11C16.12 11 15 12.12 15 13.5V18C15 20.21 16.79 22 19 22C21.21 22 23 20.21 23 18V14H21Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_2402">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

AttachEmail.propTypes = {
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

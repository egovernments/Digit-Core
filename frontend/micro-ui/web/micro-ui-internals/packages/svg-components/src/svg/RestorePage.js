import React from "react";
import PropTypes from "prop-types";

export const RestorePage = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_852)">
        <path
          d="M14 2H6C4.9 2 4.01 2.9 4.01 4L4 20C4 21.1 4.89 22 5.99 22H18C19.1 22 20 21.1 20 20V8L14 2ZM12 18C9.95 18 8.19 16.76 7.42 15H9.13C9.76 15.9 10.81 16.5 12 16.5C13.93 16.5 15.5 14.93 15.5 13C15.5 11.07 13.93 9.5 12 9.5C10.65 9.5 9.48 10.28 8.9 11.4L10.5 13H6.5V9L7.8 10.3C8.69 8.92 10.23 8 12 8C14.76 8 17 10.24 17 13C17 15.76 14.76 18 12 18Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_852">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



RestorePage.propTypes = {
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

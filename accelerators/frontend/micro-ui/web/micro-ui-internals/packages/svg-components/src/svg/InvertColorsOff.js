import React from "react";
import PropTypes from "prop-types";

export const InvertColorsOff = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_2103)">
        <path
          d="M20.65 20.87L18.3 18.52L12 12.23L8.44 8.66002L7.02 7.25002L4.27 4.50002L3 5.77002L5.78 8.55002C3.23 11.69 3.42 16.31 6.34 19.24C7.9 20.8 9.95 21.58 12 21.58C13.79 21.58 15.57 20.99 17.03 19.8L19.73 22.5L21 21.23L20.65 20.87ZM12 19.59C10.4 19.59 8.89 18.97 7.76 17.83C6.62 16.69 6 15.19 6 13.59C6 12.27 6.43 11.02 7.21 9.99002L12 14.77V19.59ZM12 5.10002V9.68002L19.25 16.94C20.62 13.98 20.09 10.37 17.65 7.93002L12 2.27002L8.3 5.97002L9.71 7.38002L12 5.10002Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_2103">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



InvertColorsOff.propTypes = {
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

import React from "react";
import PropTypes from "prop-types";

export const InvertColors = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_544)">
        <path
          d="M17.66 7.93002L12 2.27002L6.34 7.93002C3.22 11.05 3.22 16.12 6.34 19.24C7.9 20.8 9.95 21.58 12 21.58C14.05 21.58 16.1 20.8 17.66 19.24C20.78 16.12 20.78 11.05 17.66 7.93002V7.93002ZM12 19.59C10.4 19.59 8.89 18.97 7.76 17.83C6.62 16.69 6 15.19 6 13.59C6 11.99 6.62 10.48 7.76 9.35002L12 5.10002V19.59Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_544">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



InvertColors.propTypes = {
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

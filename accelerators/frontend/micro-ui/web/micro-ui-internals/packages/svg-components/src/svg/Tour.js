import React from "react";
import PropTypes from "prop-types";

export const Tour = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1116)">
        <path
          d="M21 4H7V2H5V22H7V14H21L19 9L21 4ZM15 9C15 10.1 14.1 11 13 11C11.9 11 11 10.1 11 9C11 7.9 11.9 7 13 7C14.1 7 15 7.9 15 9Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_1116">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

Tour.propTypes = {
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

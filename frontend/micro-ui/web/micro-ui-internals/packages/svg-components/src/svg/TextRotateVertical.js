import React from "react";
import PropTypes from "prop-types";

export const TextRotateVertical = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1059)">
        <path
          d="M15.75 5H14.25L9.5 16H11.6L12.5 13.8H17.5L18.4 16H20.5L15.75 5ZM13.13 12L15 6.98L16.87 12H13.13V12ZM6 19.75L9 16.75H7V4.25H5V16.75H3L6 19.75Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_1059">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

TextRotateVertical.propTypes = {
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

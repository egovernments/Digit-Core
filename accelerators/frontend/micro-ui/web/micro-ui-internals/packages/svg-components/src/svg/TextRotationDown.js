import React from "react";
import PropTypes from "prop-types";

export const TextRotationDown = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1068)">
        <path
          d="M21 12V10.5L10 5.75V7.85L12.2 8.75V13.75L10 14.65V16.75L21 12ZM14 9.38L19.02 11.25L14 13.12V9.38V9.38ZM6 19.75L9 16.75H7V4.25H5V16.75H3L6 19.75Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_1068">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

TextRotationDown.propTypes = {
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

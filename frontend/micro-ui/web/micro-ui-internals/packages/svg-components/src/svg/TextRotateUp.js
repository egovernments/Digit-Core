import React from "react";
import PropTypes from "prop-types";

export const TextRotateUp = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1056)">
        <path
          d="M3 12V13.5L14 18.25V16.15L11.8 15.25V10.25L14 9.35V7.25L3 12ZM10 14.62L4.98 12.75L10 10.88V14.62ZM18 4.25L15 7.25H17V19.75H19V7.25H21L18 4.25V4.25Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_1056">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

TextRotateUp.propTypes = {
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

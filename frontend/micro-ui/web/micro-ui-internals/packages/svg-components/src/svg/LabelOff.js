import React from "react";
import PropTypes from "prop-types";

export const LabelOff = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_556)">
        <path
          d="M3.25 2.75L20.25 19.75L19 21L17 19H5C3.9 19 3 18.1 3 17V7C3 6.45 3.23 5.95 3.59 5.59L2 4L3.25 2.75ZM22 12L17.63 5.84C17.27 5.33 16.67 5 16 5H8L19 16L22 12Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_556">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



LabelOff.propTypes = {
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

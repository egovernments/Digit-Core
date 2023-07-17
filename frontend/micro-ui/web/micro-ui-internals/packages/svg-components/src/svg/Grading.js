import React from "react";
import PropTypes from "prop-types";

export const Grading = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_462)">
        <path
          d="M4 7H20V9H4V7ZM4 13H20V11H4V13ZM4 17H11V15H4V17ZM4 21H11V19H4V21ZM15.41 18.17L14 16.75L12.59 18.16L15.41 21L20 16.42L18.58 15L15.41 18.17ZM4 3V5H20V3H4Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_462">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



Grading.propTypes = {
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

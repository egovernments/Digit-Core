import React from "react";
import PropTypes from "prop-types";

export const LabelImportant = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_550)">
        <path
          d="M3.5 18.99L14.5 19C15.17 19 15.77 18.67 16.13 18.16L20.5 12L16.13 5.84C15.77 5.33 15.17 5 14.5 5L3.5 5.01L8.34 12L3.5 18.99Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_550">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



LabelImportant.propTypes = {
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

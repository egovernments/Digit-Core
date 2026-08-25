import React from "react";
import PropTypes from "prop-types";

export const FitScreen = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_411)">
        <path
          d="M17 4H20C21.1 4 22 4.9 22 6V8H20V6H17V4ZM4 8V6H7V4H4C2.9 4 2 4.9 2 6V8H4ZM20 16V18H17V20H20C21.1 20 22 19.1 22 18V16H20ZM7 18H4V16H2V18C2 19.1 2.9 20 4 20H7V18ZM18 8H6V16H18V8Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_411">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



FitScreen.propTypes = {
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

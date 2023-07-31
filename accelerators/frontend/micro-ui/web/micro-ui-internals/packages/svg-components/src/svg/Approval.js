import React from "react";
import PropTypes from "prop-types";

export const Approval = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_2396)">
        <path
          d="M4 16V22H20V16C20 14.9 19.1 14 18 14H6C4.9 14 4 14.9 4 16ZM18 18H6V16H18V18ZM12 2C9.24 2 7 4.24 7 7L12 14L17 7C17 4.24 14.76 2 12 2ZM12 11L9 7C9 5.34 10.34 4 12 4C13.66 4 15 5.34 15 7L12 11Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_2396">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

Approval.propTypes = {
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

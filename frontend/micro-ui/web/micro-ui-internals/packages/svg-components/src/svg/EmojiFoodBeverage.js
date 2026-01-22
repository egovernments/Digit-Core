import React from "react";
import PropTypes from "prop-types";

export const EmojiFoodBeverage = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_176_641)">
        <path
          d="M20 3H9V5.4L10.81 6.85C10.93 6.94 11 7.09 11 7.24V11.5C11 11.78 10.78 12 10.5 12H6.5C6.22 12 6 11.78 6 11.5V7.24C6 7.09 6.07 6.94 6.19 6.85L8 5.4V3H4V13C4 15.21 5.79 17 8 17H14C16.21 17 18 15.21 18 13V10H20C21.11 10 22 9.1 22 8V5C22 3.89 21.11 3 20 3ZM20 8H18V5H20V8Z"
          fill={fill}
        />
        <path d="M20 19H4V21H20V19Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_176_641">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



EmojiFoodBeverage.propTypes = {
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

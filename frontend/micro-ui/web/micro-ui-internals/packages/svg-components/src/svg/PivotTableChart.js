import React from "react";
import PropTypes from "prop-types";

export const PivotTableChart = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1576)">
        <path
          d="M10 8H21V5C21 3.9 20.1 3 19 3H10V8ZM3 8H8V3H5C3.9 3 3 3.9 3 5V8ZM5 21H8V10H3V19C3 20.1 3.9 21 5 21ZM13 22L9 18L13 14V22ZM14 13L18 9L22 13H14Z"
          fill={fill}
        />
        <path d="M14.58 19H13V17H14.58C15.91 17 17 15.92 17 14.58V13H19V14.58C19 17.02 17.02 19 14.58 19Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_105_1576">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



PivotTableChart.propTypes = {
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

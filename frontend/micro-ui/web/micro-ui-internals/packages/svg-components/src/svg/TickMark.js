import React from "react";
import PropTypes from "prop-types";

export const TickMark = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => (
  <svg
    style={{ ...style }}
    className={className}
    width={width}
    height={height}
    onClick={onClick}
    viewBox="0 0 14 11"
    fill="none"
    xmlns="http://www.w3.org/2000/svg"
  >
    <path d="M4.75012 8.1275L1.62262 5L0.557617 6.0575L4.75012 10.25L13.7501 1.25L12.6926 0.192505L4.75012 8.1275Z" fill={fill} />
  </svg>
);

TickMark.propTypes = {
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

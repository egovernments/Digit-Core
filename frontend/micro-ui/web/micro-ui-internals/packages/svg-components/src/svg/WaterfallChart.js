import React from "react";
import PropTypes from "prop-types";

export const WaterfallChart = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1610)">
        <path d="M18 4H21V20H18V4ZM3 13H6V20H3V13ZM14 4H17V7H14V4ZM10 5H13V9H10V5ZM7 10H10V14H7V10Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_105_1610">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

WaterfallChart.propTypes = {
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

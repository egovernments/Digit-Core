import React from "react";
import PropTypes from "prop-types";

export const PlayForWork = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_775)">
        <path
          d="M11 5V10.59H7.5L12 15.09L16.5 10.59H13V5H11ZM6 14C6 17.31 8.69 20 12 20C15.31 20 18 17.31 18 14H16C16 16.21 14.21 18 12 18C9.79 18 8 16.21 8 14H6Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_775">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



PlayForWork.propTypes = {
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

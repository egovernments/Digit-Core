import React from "react";
import PropTypes from "prop-types";

export const CallSplit = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_2015)">
        <path
          d="M14 4L16.29 6.29L13.41 9.17L14.83 10.59L17.71 7.71L20 10V4H14ZM10 4H4V10L6.29 7.71L11 12.41V20H13V11.59L7.71 6.29L10 4Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_2015">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



CallSplit.propTypes = {
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

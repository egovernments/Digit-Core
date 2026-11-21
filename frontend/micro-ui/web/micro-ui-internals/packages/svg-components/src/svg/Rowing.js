import React from "react";
import PropTypes from "prop-types";

export const Rowing = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_865)">
        <path
          d="M8.5 14.5L4 19L5.5 20.5L9 17H11L8.5 14.5ZM15 1C13.9 1 13 1.9 13 3C13 4.1 13.9 5 15 5C16.1 5 17 4.1 17 3C17 1.9 16.1 1 15 1ZM21 21.01L18 24L15.01 20.99V19.5L7.91 12.41C7.6 12.46 7.3 12.48 7 12.48V10.32C8.66 10.35 10.61 9.45 11.67 8.28L13.07 6.73C13.42 6.34 14.06 6 14.72 6H14.75C15.99 6.01 17 7.02 17 8.26V14.01C17 14.85 16.65 15.62 16.08 16.17L12.5 12.59V10.32C11.87 10.84 11.07 11.34 10.21 11.71L16.5 18H18L21 21.01Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_865">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



Rowing.propTypes = {
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

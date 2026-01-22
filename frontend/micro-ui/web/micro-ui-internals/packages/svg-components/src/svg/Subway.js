import React from "react";
import PropTypes from "prop-types";

export const Subway = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11372)">
        <path
          d="M15.5 17C16.0523 17 16.5 16.5523 16.5 16C16.5 15.4477 16.0523 15 15.5 15C14.9477 15 14.5 15.4477 14.5 16C14.5 16.5523 14.9477 17 15.5 17Z"
          fill={fill}
        />
        <path
          d="M8.5 17C9.05228 17 9.5 16.5523 9.5 16C9.5 15.4477 9.05228 15 8.5 15C7.94772 15 7.5 15.4477 7.5 16C7.5 16.5523 7.94772 17 8.5 17Z"
          fill={fill}
        />
        <path
          d="M7.01 9H17.01V14H7.01V9ZM17.8 2.8C16 2.09 13.86 2 12 2C10.14 2 8 2.09 6.2 2.8C3.53 3.84 2 6.05 2 8.86V22H22V8.86C22 6.05 20.47 3.84 17.8 2.8ZM18 15.88C18 17.33 16.82 18.5 15.37 18.5L16.5 19.62V20H15L13.5 18.5H10.67L9.17 20H7.5V19.62L8.62 18.5C7.18 18.5 6 17.32 6 15.88V9C6 6.37 9 6 12 6C15.32 6 18 6.38 18 9V15.88Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_11372">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



Subway.propTypes = {
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

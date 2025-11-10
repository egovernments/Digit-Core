import React from "react";
import PropTypes from "prop-types";

export const Badge = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_10833)">
        <path
          d="M20 7H15V4C15 2.9 14.1 2 13 2H11C9.9 2 9 2.9 9 4V7H4C2.9 7 2 7.9 2 9V20C2 21.1 2.9 22 4 22H20C21.1 22 22 21.1 22 20V9C22 7.9 21.1 7 20 7ZM9 12C9.83 12 10.5 12.67 10.5 13.5C10.5 14.33 9.83 15 9 15C8.17 15 7.5 14.33 7.5 13.5C7.5 12.67 8.17 12 9 12ZM12 18H6V17.25C6 16.25 8 15.75 9 15.75C10 15.75 12 16.25 12 17.25V18ZM13 9H11V4H13V9ZM18 16.5H14V15H18V16.5ZM18 13.5H14V12H18V13.5Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_10833">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



Badge.propTypes = {
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

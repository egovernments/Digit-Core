import React from "react";
import PropTypes from "prop-types";

export const ElectricMoped = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11000)">
        <path
          d="M19 5C19 3.9 18.1 3 17 3H14V5H17V7.65L13.52 12H10V7H6C3.79 7 2 8.79 2 11V14H4C4 15.66 5.34 17 7 17C8.66 17 10 15.66 10 14H14.48L19 8.35V5ZM7 15C6.45 15 6 14.55 6 14H8C8 14.55 7.55 15 7 15Z"
          fill={fill}
        />
        <path d="M10 4H5V6H10V4Z" fill={fill} />
        <path
          d="M19 11C17.34 11 16 12.34 16 14C16 15.66 17.34 17 19 17C20.66 17 22 15.66 22 14C22 12.34 20.66 11 19 11ZM19 15C18.45 15 18 14.55 18 14C18 13.45 18.45 13 19 13C19.55 13 20 13.45 20 14C20 14.55 19.55 15 19 15Z"
          fill={fill}
        />
        <path d="M7 20H11V18L17 21H13V23L7 20Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_1974_11000">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



ElectricMoped.propTypes = {
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

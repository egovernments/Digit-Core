import React from "react";
import PropTypes from "prop-types";

export const AppBlocking = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_94)">
        <path
          d="M18 8C15.79 8 14 9.79 14 12C14 14.21 15.79 16 18 16C20.21 16 22 14.21 22 12C22 9.79 20.21 8 18 8ZM15.5 12C15.5 10.62 16.62 9.5 18 9.5C18.42 9.5 18.8 9.61 19.15 9.79L15.79 13.15C15.61 12.8 15.5 12.42 15.5 12V12ZM18 14.5C17.58 14.5 17.2 14.39 16.85 14.21L20.21 10.85C20.39 11.2 20.5 11.58 20.5 12C20.5 13.38 19.38 14.5 18 14.5V14.5ZM17 18H7V6H17V7H19V3C19 1.9 18.1 1 17 1H7C5.9 1 5 1.9 5 3V21C5 22.1 5.9 23 7 23H17C18.1 23 19 22.1 19 21V17H17V18Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_94">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

AppBlocking.propTypes = {
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

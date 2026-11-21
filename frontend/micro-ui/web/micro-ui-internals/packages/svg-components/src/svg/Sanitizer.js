import React from "react";
import PropTypes from "prop-types";

export const Sanitizer = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_176_876)">
        <path
          d="M15.5 6.5C15.5 5.66 17 4 17 4C17 4 18.5 5.66 18.5 6.5C18.5 7.33 17.83 8 17 8C16.17 8 15.5 7.33 15.5 6.5ZM19.5 15C20.88 15 22 13.88 22 12.5C22 10.83 19.5 8 19.5 8C19.5 8 17 10.83 17 12.5C17 13.88 18.12 15 19.5 15ZM13 14H11V12H9V14H7V16H9V18H11V16H13V14ZM16 12V22H4V12C4 9.03 6.16 6.57 9 6.09V4H7V2H13C14.13 2 15.15 2.39 15.99 3.01L14.56 4.44C14.1 4.17 13.57 4 13 4H11V6.09C13.84 6.57 16 9.03 16 12Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_176_876">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



Sanitizer.propTypes = {
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

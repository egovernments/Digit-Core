import React from "react";
import PropTypes from "prop-types";

export const Elderly = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_176_625)">
        <path
          d="M13.5 5.5C14.6 5.5 15.5 4.6 15.5 3.5C15.5 2.4 14.6 1.5 13.5 1.5C12.4 1.5 11.5 2.4 11.5 3.5C11.5 4.6 12.4 5.5 13.5 5.5ZM20 12.5V23H19V12.5C19 12.22 18.78 12 18.5 12C18.22 12 18 12.22 18 12.5V13.5H17V12.81C15.54 12.43 14.3 11.52 13.49 10.29C13.18 11.16 13 12.07 13 13C13 13.23 13.02 13.46 13.03 13.69L15 16.5V23H13V18L11.22 15.46L11 19L8 23L6.4 21.8L9 18.33V13C9 11.85 9.18 10.71 9.5 9.61L8 10.46V14H6V9.3L11.4 6.23V6.24C11.99 5.93 12.72 5.91 13.34 6.27C13.7 6.48 13.97 6.78 14.14 7.12L14.93 8.79C15.58 10.1 16.94 11 18.5 11C19.33 11 20 11.67 20 12.5Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_176_625">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



Elderly.propTypes = {
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

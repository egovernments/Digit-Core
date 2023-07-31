import React from "react";
import PropTypes from "prop-types";

export const GTranslate = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_470)">
        <path
          d="M21 4H11L10 1H3C1.9 1 1 1.9 1 3V18C1 19.1 1.9 20 3 20H11L12 23H21C22.1 23 23 22.1 23 21V6C23 4.9 22.1 4 21 4ZM7 16C4.24 16 2 13.76 2 11C2 8.24 4.24 6 7 6C8.35 6 9.48 6.5 10.35 7.3L9.03 8.57C8.65 8.21 7.99 7.79 7 7.79C5.26 7.79 3.85 9.23 3.85 11C3.85 12.77 5.26 14.21 7 14.21C9.01 14.21 9.84 12.77 9.92 11.8H7V10.09H11.68C11.75 10.4 11.8 10.7 11.8 11.11C11.8 13.97 9.89 16 7 16ZM13.17 10.58H16.87C16.44 11.83 15.76 13.01 14.82 14.05C14.51 13.7 14.22 13.33 13.96 12.95L13.17 10.58V10.58ZM21.5 20.5C21.5 21.05 21.05 21.5 20.5 21.5H14L16 19L14.96 15.9L18.06 19L18.98 18.08L15.68 14.83L15.7 14.81C16.83 13.56 17.63 12.12 18.1 10.59H20V9.29H15.47V8H14.18V9.29H12.74L11.46 5.5H20.5C21.05 5.5 21.5 5.95 21.5 6.5V20.5Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_470">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



GTranslate.propTypes = {
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

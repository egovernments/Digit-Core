import React from "react";
import PropTypes from "prop-types";

export const PestControl = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11311)">
        <path
          d="M21 14.9998V12.9998H17.93C17.88 12.6098 17.81 12.2298 17.71 11.8598L20.29 10.3698L19.29 8.63977L16.92 9.99977C16.64 9.51977 16.3 9.08977 15.93 8.70977C15.97 8.47977 16 8.24977 16 7.99977C16 7.19977 15.76 6.44977 15.35 5.81977L17 4.16977L15.59 2.75977L13.87 4.47977C12.19 3.58977 10.77 4.14977 10.14 4.47977L8.41 2.75977L7 4.16977L8.65 5.81977C8.24 6.44977 8 7.19977 8 7.99977C8 8.24977 8.03 8.47977 8.07 8.71977C7.7 9.09977 7.36 9.52977 7.08 9.99977L4.71 8.62977L3.71 10.3598L6.29 11.8498C6.19 12.2198 6.12 12.5998 6.07 12.9898H3V14.9898H6.07C6.12 15.3798 6.19 15.7598 6.29 16.1298L3.71 17.6198L4.71 19.3498L7.08 17.9998C8.16 19.8098 9.96 20.9998 12 20.9998C14.04 20.9998 15.84 19.8098 16.92 17.9998L19.29 19.3698L20.29 17.6398L17.71 16.1498C17.81 15.7798 17.88 15.3998 17.93 15.0098H21V14.9998ZM13 16.9998H11V10.9998H13V16.9998Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_11311">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



PestControl.propTypes = {
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

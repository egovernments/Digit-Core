import React from "react";
import PropTypes from "prop-types";

export const VoiceOverOff = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1211)">
        <path
          d="M12.99 9.18C12.99 9.12 13 9.06 13 9C13 6.79 11.21 5 9 5C8.94 5 8.88 5.01 8.82 5.01L12.99 9.18ZM6.89 5.62L4.27 3L3 4.27L5.62 6.89C5.23 7.5 5 8.22 5 9C5 11.21 6.79 13 9 13C9.78 13 10.5 12.77 11.11 12.38L19.73 21L21 19.73L12.38 11.11L6.89 5.62V5.62ZM9 15C6.33 15 1 16.34 1 19V21H17V19C17 16.34 11.67 15 9 15ZM16.76 5.36L15.08 7.05C15.92 8.23 15.92 9.76 15.08 10.94L16.76 12.63C18.78 10.61 18.78 7.56 16.76 5.36ZM20.07 2L18.44 3.63C21.21 6.65 21.21 11.19 18.44 14.37L20.07 16C23.97 12.11 23.98 6.05 20.07 2Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_1211">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

VoiceOverOff.propTypes = {
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

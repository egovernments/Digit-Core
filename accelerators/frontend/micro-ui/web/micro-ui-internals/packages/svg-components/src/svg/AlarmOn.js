import React from "react";
import PropTypes from "prop-types";

export const AlarmOn = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_67)">
        <path
          d="M22 5.72011L17.4 1.86011L16.11 3.39011L20.71 7.25011L22 5.72011ZM7.88 3.39011L6.6 1.86011L2 5.71011L3.29 7.24011L7.88 3.39011ZM12 4.00011C7.03 4.00011 3 8.03011 3 13.0001C3 17.9701 7.02 22.0001 12 22.0001C16.97 22.0001 21 17.9701 21 13.0001C21 8.03011 16.97 4.00011 12 4.00011ZM12 20.0001C8.13 20.0001 5 16.8701 5 13.0001C5 9.13011 8.13 6.00011 12 6.00011C15.87 6.00011 19 9.13011 19 13.0001C19 16.8701 15.87 20.0001 12 20.0001ZM10.54 14.5301L8.41 12.4001L7.35 13.4601L10.53 16.6401L16.53 10.6401L15.47 9.58011L10.54 14.5301V14.5301Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_67">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

AlarmOn.propTypes = {
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

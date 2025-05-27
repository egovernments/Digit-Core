import React from "react";
import PropTypes from "prop-types";

export const PhonelinkLock = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_2184)">
        <path
          d="M19 1H9C7.9 1 7 1.9 7 3V6H9V4H19V20H9V18H7V21C7 22.1 7.9 23 9 23H19C20.1 23 21 22.1 21 21V3C21 1.9 20.1 1 19 1ZM10.8 11V9.5C10.8 8.1 9.4 7 8 7C6.6 7 5.2 8.1 5.2 9.5V11C4.6 11 4 11.6 4 12.2V15.7C4 16.4 4.6 17 5.2 17H10.7C11.4 17 12 16.4 12 15.8V12.3C12 11.6 11.4 11 10.8 11ZM9.5 11H6.5V9.5C6.5 8.7 7.2 8.2 8 8.2C8.8 8.2 9.5 8.7 9.5 9.5V11Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_2184">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



PhonelinkLock.propTypes = {
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

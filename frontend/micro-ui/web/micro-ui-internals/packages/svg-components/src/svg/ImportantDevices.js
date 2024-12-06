import React from "react";
import PropTypes from "prop-types";

export const ImportantDevices = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_524)">
        <path
          d="M23 11.01L18 11C17.45 11 17 11.45 17 12V21C17 21.55 17.45 22 18 22H23C23.55 22 24 21.55 24 21V12C24 11.45 23.55 11.01 23 11.01ZM23 20H18V13H23V20ZM20 2H2C0.89 2 0 2.89 0 4V16C0 17.1 0.89 18 2 18H9V20H7V22H15V20H13V18H15V16H2V4H20V9H22V4C22 2.89 21.1 2 20 2ZM11.97 9L11 6L10.03 9H7L9.47 10.76L8.53 13.67L11 11.87L13.47 13.67L12.53 10.76L15 9H11.97Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_524">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



ImportantDevices.propTypes = {
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

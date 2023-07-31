import React from "react";
import PropTypes from "prop-types";

export const CancelScheduleSend = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_193)">
        <path
          d="M16.5 9C16.08 9 15.67 9.04 15.26 9.11L1.01 3L1 10L10 12L1 14L1.01 21L9.08 17.54C9.59 21.19 12.71 24 16.5 24C20.64 24 24 20.64 24 16.5C24 12.36 20.64 9 16.5 9ZM16.5 22C13.47 22 11 19.53 11 16.5C11 13.47 13.47 11 16.5 11C19.53 11 22 13.47 22 16.5C22 19.53 19.53 22 16.5 22Z"
          fill={fill}
        />
        <path
          d="M18.2703 14.03L16.5003 15.79L14.7303 14.03L14.0303 14.73L15.7903 16.5L14.0303 18.27L14.7303 18.97L16.5003 17.21L18.2703 18.97L18.9703 18.27L17.2103 16.5L18.9703 14.73L18.2703 14.03Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_193">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



CancelScheduleSend.propTypes = {
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

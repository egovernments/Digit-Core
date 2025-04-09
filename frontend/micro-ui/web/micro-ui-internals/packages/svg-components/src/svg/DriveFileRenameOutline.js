import React from "react";
import PropTypes from "prop-types";

export const DriveFileRenameOutline = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_2439)">
        <path
          d="M18.41 5.79988L17.2 4.58988C16.42 3.80988 15.15 3.80988 14.37 4.58988L11.69 7.26988L3 15.9599V19.9999H7.04L15.78 11.2599L18.41 8.62988C19.2 7.84988 19.2 6.57988 18.41 5.79988V5.79988ZM6.21 17.9999H5V16.7899L13.66 8.12988L14.87 9.33988L6.21 17.9999ZM11 19.9999L15 15.9999H21V19.9999H11Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_2439">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



DriveFileRenameOutline.propTypes = {
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

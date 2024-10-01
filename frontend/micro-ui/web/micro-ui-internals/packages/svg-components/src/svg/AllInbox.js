import React from "react";
import PropTypes from "prop-types";

export const AllInbox = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_70)">
        <path
          d="M19 3H5C3.9 3 3 3.9 3 5V12C3 13.1 3.9 14 5 14H19C20.1 14 21 13.1 21 12V5C21 3.9 20.1 3 19 3ZM19 9H15C15 10.62 13.62 12 12 12C10.38 12 9 10.62 9 9H5V5H19V9ZM15 16H21V19C21 20.1 20.1 21 19 21H5C3.9 21 3 20.1 3 19V16H9C9 17.66 10.34 19 12 19C13.66 19 15 17.66 15 16Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_70">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

AllInbox.propTypes = {
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

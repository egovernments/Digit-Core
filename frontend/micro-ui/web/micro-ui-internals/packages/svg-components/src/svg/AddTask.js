import React from "react";
import PropTypes from "prop-types";

export const AddTask = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_45)">
        <path
          d="M22 5.18L10.59 16.6L6.35 12.36L7.76 10.95L10.59 13.78L20.59 3.78L22 5.18ZM12 20C7.59 20 4 16.41 4 12C4 7.59 7.59 4 12 4C13.57 4 15.04 4.46 16.28 5.25L17.73 3.8C16.1 2.67 14.13 2 12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C13.73 22 15.36 21.56 16.78 20.78L15.28 19.28C14.28 19.74 13.17 20 12 20ZM19 15H16V17H19V20H21V17H24V15H21V12H19V15Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_45">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

AddTask.propTypes = {
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

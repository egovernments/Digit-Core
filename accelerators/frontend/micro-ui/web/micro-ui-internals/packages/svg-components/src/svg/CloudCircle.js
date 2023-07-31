import React from "react";
import PropTypes from "prop-types";

export const CloudCircle = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_2412)">
        <path
          d="M12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 12 2ZM16.5 16H8C6.34 16 5 14.66 5 13C5 11.34 6.34 10 8 10L8.14 10.01C8.58 8.28 10.13 7 12 7C14.21 7 16 8.79 16 11H16.5C17.88 11 19 12.12 19 13.5C19 14.88 17.88 16 16.5 16Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_2412">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



CloudCircle.propTypes = {
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

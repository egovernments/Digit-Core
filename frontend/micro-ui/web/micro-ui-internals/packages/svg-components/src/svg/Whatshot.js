import React from "react";
import PropTypes from "prop-types";

export const Whatshot = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_176_1084)">
        <path
          d="M13.5 0.669922C13.5 0.669922 14.24 3.31992 14.24 5.46992C14.24 7.52992 12.89 9.19992 10.83 9.19992C8.76 9.19992 7.2 7.52992 7.2 5.46992L7.23 5.10992C5.21 7.50992 4 10.6199 4 13.9999C4 18.4199 7.58 21.9999 12 21.9999C16.42 21.9999 20 18.4199 20 13.9999C20 8.60992 17.41 3.79992 13.5 0.669922ZM11.71 18.9999C9.93 18.9999 8.49 17.5999 8.49 15.8599C8.49 14.2399 9.54 13.0999 11.3 12.7399C13.07 12.3799 14.9 11.5299 15.92 10.1599C16.31 11.4499 16.51 12.8099 16.51 14.1999C16.51 16.8499 14.36 18.9999 11.71 18.9999Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_176_1084">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

Whatshot.propTypes = {
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

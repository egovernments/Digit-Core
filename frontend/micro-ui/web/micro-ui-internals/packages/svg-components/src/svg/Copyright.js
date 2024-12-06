import React from "react";
import PropTypes from "prop-types";

export const Copyright = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_264)">
        <path
          d="M11.88 9.14C13.16 9.2 13.49 10.29 13.51 10.8H15.3C15.22 8.82 13.81 7.61 11.85 7.61C9.64 7.61 8 9 8 12.14C8 14.08 8.93 16.38 11.84 16.38C14.06 16.38 15.25 14.73 15.28 13.43H13.49C13.46 14.02 13.04 14.81 11.86 14.87C10.55 14.83 10 13.81 10 12.14C10 9.25 11.28 9.16 11.88 9.14ZM12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 12 2ZM12 20C7.59 20 4 16.41 4 12C4 7.59 7.59 4 12 4C16.41 4 20 7.59 20 12C20 16.41 16.41 20 12 20Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_264">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



Copyright.propTypes = {
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

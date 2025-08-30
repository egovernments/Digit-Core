import React from "react";
import PropTypes from "prop-types";

export const ScreenShareStop = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_2272)">
        <path
          d="M21.22 18.02L23.22 20.02H24V18.02H21.22ZM21.99 16.02L22 6.01998C22 4.90998 21.1 4.01998 20 4.01998H7.22L12.45 9.24998C12.63 9.20998 12.81 9.17998 13 9.14998V7.01998L17 10.75L15.42 12.22L20.96 17.76C21.57 17.43 21.99 16.77 21.99 16.02V16.02ZM2.39 1.72998L1.11 2.99998L2.65 4.53998C2.25 4.89998 2 5.42998 2 6.01998V16.02C2 17.12 2.89 18.02 4 18.02H0V20.02H18.13L20.84 22.73L22.11 21.46L2.39 1.72998ZM7 15.02C7.31 13.54 7.92 12.07 9.07 10.96L10.66 12.55C9.12 12.93 7.96 13.73 7 15.02V15.02Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_2272">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



ScreenShareStop.propTypes = {
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

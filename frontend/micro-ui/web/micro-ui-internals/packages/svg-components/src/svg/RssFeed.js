import React from "react";
import PropTypes from "prop-types";

export const RssFeed = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_2239)">
        <path
          d="M6.18 20.0001C7.38398 20.0001 8.36 19.0241 8.36 17.8201C8.36 16.6162 7.38398 15.6401 6.18 15.6401C4.97602 15.6401 4 16.6162 4 17.8201C4 19.0241 4.97602 20.0001 6.18 20.0001Z"
          fill={fill}
        />
        <path
          d="M4 4.43994V7.26994C11.03 7.26994 16.73 12.9699 16.73 19.9999H19.56C19.56 11.4099 12.59 4.43994 4 4.43994V4.43994ZM4 10.0999V12.9299C7.9 12.9299 11.07 16.0999 11.07 19.9999H13.9C13.9 14.5299 9.47 10.0999 4 10.0999V10.0999Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_2239">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



RssFeed.propTypes = {
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

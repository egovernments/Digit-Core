import React from "react";
import PropTypes from "prop-types";

export const OfflineShare = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1570)">
        <path
          d="M14.6 10.26V11.57L17 9.33L14.6 7.1V8.38C12.27 8.7 11.34 10.3 11 11.9C11.83 10.77 12.93 10.26 14.6 10.26V10.26ZM16 23H6C4.9 23 4 22.1 4 21V5H6V21H16V23ZM18 1H10C8.9 1 8 1.9 8 3V17C8 18.1 8.9 19 10 19H18C19.1 19 20 18.1 20 17V3C20 1.9 19.1 1 18 1ZM18 16H10V4H18V16Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_1570">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



OfflineShare.propTypes = {
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

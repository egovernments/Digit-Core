import React from "react";
import PropTypes from "prop-types";

export const IosShare = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_176_727)">
        <path
          d="M16 5L14.58 6.42L12.99 4.83V16H11.01V4.83L9.42 6.42L8 5L12 1L16 5ZM20 10V21C20 22.1 19.1 23 18 23H6C4.89 23 4 22.1 4 21V10C4 8.89 4.89 8 6 8H9V10H6V21H18V10H15V8H18C19.1 8 20 8.89 20 10Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_176_727">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



IosShare.propTypes = {
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

import React from "react";
import PropTypes from "prop-types";

export const Quickreply = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_812)">
        <path d="M22 4C22 2.9 21.1 2 20 2H4C2.9 2 2.01 2.9 2.01 4L2 22L6 18H15V10H22V4Z" fill={fill} />
        <path d="M22.5 16H20.3L22 12H17V18H19V23L22.5 16Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_105_812">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



Quickreply.propTypes = {
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

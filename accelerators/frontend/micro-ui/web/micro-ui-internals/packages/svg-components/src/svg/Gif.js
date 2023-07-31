import React from "react";
import PropTypes from "prop-types";

export const Gif = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_451)">
        <path d="M13 9H11.5V15H13V9Z" fill={fill} />
        <path d="M9 9H6C5.4 9 5 9.5 5 10V14C5 14.5 5.4 15 6 15H9C9.6 15 10 14.5 10 14V12H8.5V13.5H6.5V10.5H10V10C10 9.5 9.6 9 9 9Z" fill={fill} />
        <path d="M19 10.5V9H14.5V15H16V13H18V11.5H16V10.5H19Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_105_451">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



Gif.propTypes = {
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

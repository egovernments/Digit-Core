import React from "react";
import PropTypes from "prop-types";

export const DoubleArrow = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1512)">
        <path d="M15.5 5H11L16 12L11 19H15.5L20.5 12L15.5 5Z" fill={fill} />
        <path d="M8.5 5H4L9 12L4 19H8.5L13.5 12L8.5 5Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_105_1512">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



DoubleArrow.propTypes = {
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

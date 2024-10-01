import React from "react";
import PropTypes from "prop-types";

export const HomeWork = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1537)">
        <path d="M8.17 5.7002L1 10.4802V21.0002H6V13.0002H10V21.0002H15V10.2502L8.17 5.7002Z" fill={fill} />
        <path d="M10 3V4.51L12 5.84L13.73 7H15V7.85L17 9.19V11H19V13H17V15H19V17H17V21H23V3H10ZM19 9H17V7H19V9Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_105_1537">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



HomeWork.propTypes = {
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

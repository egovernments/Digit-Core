import React from "react";
import PropTypes from "prop-types";

export const PermMedia = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_746)">
        <path
          d="M2 6H0V11H0.01L0 20C0 21.1 0.9 22 2 22H20V20H2V6ZM22 4H14L12 2H6C4.9 2 4.01 2.9 4.01 4L4 16C4 17.1 4.9 18 6 18H22C23.1 18 24 17.1 24 16V6C24 4.9 23.1 4 22 4ZM7 15L11.5 9L15 13.51L17.5 10.5L21 15H7Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_746">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



PermMedia.propTypes = {
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

import React from "react";
import PropTypes from "prop-types";

export const LayersClear = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11091)">
        <path
          d="M19.81 14.99L21 14.07L19.57 12.64L18.38 13.56L19.81 14.99ZM19.36 10.27L21 9L12 2L9.09 4.27L16.96 12.15L19.36 10.27ZM3.27 1L2 2.27L6.22 6.49L3 9L4.63 10.27L12 16L14.1 14.37L15.53 15.8L12 18.54L4.63 12.81L3 14.07L12 21.07L16.95 17.22L20.73 21L22 19.73L3.27 1Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_11091">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



LayersClear.propTypes = {
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

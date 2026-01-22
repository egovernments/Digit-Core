import React from "react";
import PropTypes from "prop-types";

export const Layers = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11088)">
        <path
          d="M11.99 18.54L4.62 12.81L3 14.07L12 21.07L21 14.07L19.37 12.8L11.99 18.54V18.54ZM12 16L19.36 10.27L21 9L12 2L3 9L4.63 10.27L12 16Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_11088">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



Layers.propTypes = {
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

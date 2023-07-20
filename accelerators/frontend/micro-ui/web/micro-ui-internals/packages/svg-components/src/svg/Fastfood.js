import React from "react";
import PropTypes from "prop-types";

export const Fastfood = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11026)">
        <path
          d="M18.06 22.99H19.72C20.56 22.99 21.25 22.35 21.35 21.53L23 5.05H18V1H16.03V5.05H11.06L11.36 7.39C13.07 7.86 14.67 8.71 15.63 9.65C17.07 11.07 18.06 12.54 18.06 14.94V22.99V22.99ZM1 21.99V21H16.03V21.99C16.03 22.54 15.58 22.99 15.02 22.99H2.01C1.45 22.99 1 22.54 1 21.99V21.99ZM16.03 14.99C16.03 6.99 1 6.99 1 14.99H16.03ZM1.02 17H16.02V19H1.02V17Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_11026">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



Fastfood.propTypes = {
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

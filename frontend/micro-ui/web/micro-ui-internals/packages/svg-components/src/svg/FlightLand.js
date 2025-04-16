import React from "react";
import PropTypes from "prop-types";

export const FlightLand = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_419)">
        <path
          d="M2.5 19H21.5V21H2.5V19ZM19.34 15.85C20.14 16.06 20.96 15.59 21.18 14.79C21.39 13.99 20.92 13.17 20.12 12.95L14.81 11.53L12.05 2.51L10.12 2V10.28L5.15 8.95L4.22 6.63L2.77 6.24V11.41L19.34 15.85Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_419">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



FlightLand.propTypes = {
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

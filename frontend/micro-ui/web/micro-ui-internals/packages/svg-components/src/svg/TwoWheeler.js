import React from "react";
import PropTypes from "prop-types";

export const TwoWheeler = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11414)">
        <path
          d="M20 11C19.82 11 19.64 11.03 19.47 11.05L17.41 9H20V6L16.28 7.86L13.41 5H9V7H12.59L14.59 9H11L7 11L5 9H0V11H4C1.79 11 0 12.79 0 15C0 17.21 1.79 19 4 19C6.21 19 8 17.21 8 15L10 17H13L16.49 10.9L17.5 11.91C16.59 12.64 16 13.75 16 15C16 17.21 17.79 19 20 19C22.21 19 24 17.21 24 15C24 12.79 22.21 11 20 11ZM4 17C2.9 17 2 16.1 2 15C2 13.9 2.9 13 4 13C5.1 13 6 13.9 6 15C6 16.1 5.1 17 4 17ZM20 17C18.9 17 18 16.1 18 15C18 13.9 18.9 13 20 13C21.1 13 22 13.9 22 15C22 16.1 21.1 17 20 17Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_11414">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

TwoWheeler.propTypes = {
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

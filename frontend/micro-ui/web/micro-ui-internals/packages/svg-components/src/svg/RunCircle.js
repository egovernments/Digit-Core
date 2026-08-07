import React from "react";
import PropTypes from "prop-types";

export const RunCircle = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11353)">
        <path
          d="M12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 12 2ZM13.5 6C14.05 6 14.5 6.45 14.5 7C14.5 7.55 14.05 8 13.5 8C12.95 8 12.5 7.55 12.5 7C12.5 6.45 12.95 6 13.5 6ZM16 12C15.3 12 13.99 11.46 13.09 10.24L12.68 12.59L14 14.03V18H13V14.42L11.89 13.21L11.37 15.85L7.6 15.08L7.8 14.1L10.58 14.67L11.54 9.78L10 10.35V12H9V9.65L12.28 8.44C12.77 8.26 13.31 8.5 13.54 8.97C14.37 10.67 15.59 11 16 11V12Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_11353">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



RunCircle.propTypes = {
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

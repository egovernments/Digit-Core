import React from "react";
import PropTypes from "prop-types";

export const CarRepair = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_10874)">
        <path
          d="M16.22 12C16.9 12 17.44 11.46 17.44 10.78C17.44 10.11 16.9 9.56 16.22 9.56C15.54 9.56 15 10.11 15 10.78C15 11.46 15.55 12 16.22 12ZM6.56 10.78C6.56 11.45 7.1 12 7.78 12C8.46 12 9 11.46 9 10.78C9 10.11 8.46 9.56 7.78 9.56C7.1 9.56 6.56 10.11 6.56 10.78ZM7.61 4L6.28 8H17.71L16.38 4H7.61ZM16.28 3C16.28 3 16.82 3.01 17.2 3.54C17.22 3.56 17.23 3.58 17.25 3.61C17.32 3.72 17.39 3.85 17.44 4.01C17.66 4.66 19 8.69 19 8.69V15.19C19 15.64 18.65 16 18.22 16H17.78C17.35 16 17 15.64 17 15.19V14H7V15.19C7 15.64 6.65 16 6.22 16H5.78C5.35 16 5 15.64 5 15.19V8.69C5 8.69 6.34 4.67 6.55 4C6.6 3.84 6.67 3.72 6.74 3.6C6.77 3.58 6.78 3.56 6.8 3.54C7.18 3.01 7.72 3 7.72 3H16.28ZM4 17.01H20V19H13V22H11V19H4V17.01Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_10874">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



CarRepair.propTypes = {
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

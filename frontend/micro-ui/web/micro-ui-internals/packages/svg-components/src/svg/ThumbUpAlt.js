import React from "react";
import PropTypes from "prop-types";

export const ThumbUpAlt = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_176_1081)">
        <path
          d="M2 20H4C4.55 20 5 19.55 5 19V10C5 9.45 4.55 9 4 9H2V20ZM21.83 12.88C21.94 12.63 22 12.36 22 12.08V11C22 9.9 21.1 9 20 9H14.5L15.42 4.35C15.47 4.13 15.44 3.89 15.34 3.69C15.11 3.24 14.82 2.83 14.46 2.47L14 2L7.59 8.41C7.21 8.79 7 9.3 7 9.83V17.67C7 18.95 8.05 20 9.34 20H17.45C18.15 20 18.81 19.63 19.17 19.03L21.83 12.88V12.88Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_176_1081">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

ThumbUpAlt.propTypes = {
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

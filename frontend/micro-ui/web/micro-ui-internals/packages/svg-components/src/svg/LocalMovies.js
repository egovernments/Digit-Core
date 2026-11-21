import React from "react";
import PropTypes from "prop-types";

export const LocalMovies = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11160)">
        <path
          d="M18 3V5H16V3H8V5H6V3H4V21H6V19H8V21H16V19H18V21H20V3H18ZM8 17H6V15H8V17ZM8 13H6V11H8V13ZM8 9H6V7H8V9ZM18 17H16V15H18V17ZM18 13H16V11H18V13ZM18 9H16V7H18V9Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_11160">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



LocalMovies.propTypes = {
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

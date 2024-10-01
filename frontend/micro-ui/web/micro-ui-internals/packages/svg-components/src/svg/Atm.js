import React from "react";
import PropTypes from "prop-types";

export const Atm = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_10825)">
        <path
          d="M8 9V10.5H10.25V15H11.75V10.5H14V9H8ZM6 9H3C2.45 9 2 9.45 2 10V15H3.5V13.5H5.5V15H7V10C7 9.45 6.55 9 6 9ZM5.5 12H3.5V10.5H5.5V12ZM21 9H16.5C15.95 9 15.5 9.45 15.5 10V15H17V10.5H18V14H19.5V10.49H20.5V15H22V10C22 9.45 21.55 9 21 9Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_10825">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

Atm.propTypes = {
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

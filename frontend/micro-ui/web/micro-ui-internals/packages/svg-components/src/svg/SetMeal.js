import React from "react";
import PropTypes from "prop-types";

export const SetMeal = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11361)">
        <path
          d="M21.05 17.56L3.08 18.5L3 17L20.98 16.06L21.05 17.56ZM21 19.48H3V20.98H21V19.48ZM22 5V12C22 13.1 21.1 14 20 14H4C2.9 14 2 13.1 2 12V5C2 3.9 2.9 3 4 3H20C21.1 3 22 3.9 22 5ZM20 6C18.32 6 16.96 6.98 16.79 8.23C16.15 7.5 14.06 5.5 10.25 5.5C5.58 5.5 3.5 8.5 3.5 8.5C3.5 8.5 5.58 11.5 10.25 11.5C14.06 11.5 16.15 9.5 16.79 8.77C16.96 10.02 18.32 11 20 11V6Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_11361">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



SetMeal.propTypes = {
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

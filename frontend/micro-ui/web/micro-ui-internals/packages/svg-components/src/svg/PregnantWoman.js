import React from "react";
import PropTypes from "prop-types";

export const PregnantWoman = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_784)">
        <path
          d="M9 4C9 2.89 9.89 2 11 2C12.11 2 13 2.89 13 4C13 5.11 12.11 6 11 6C9.89 6 9 5.11 9 4ZM16 13C15.99 11.66 15.17 10.49 14 10C14 8.34 12.66 7 11 7C9.34 7 8 8.34 8 10V17H10V22H13V17H16V13Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_784">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



PregnantWoman.propTypes = {
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

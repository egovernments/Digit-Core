import React from "react";
import PropTypes from "prop-types";

export const Mediation = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_635)">
        <path
          d="M22 12L18 16L16.59 14.59L18.17 13H12.94C12.6 16.1 10.68 18.72 8 20.05C7.96 21.69 6.64 23 5 23C3.34 23 2 21.66 2 20C2 18.34 3.34 17 5 17C5.95 17 6.78 17.45 7.33 18.14C9.23 17.11 10.59 15.23 10.91 13H7.81C7.4 14.16 6.3 15 5 15C3.34 15 2 13.66 2 12C2 10.34 3.34 9 5 9C6.3 9 7.4 9.84 7.82 11H10.92C10.6 8.77 9.23 6.9 7.33 5.86C6.78 6.55 5.95 7 5 7C3.34 7 2 5.66 2 4C2 2.34 3.34 1 5 1C6.64 1 7.96 2.31 7.99 3.95C10.67 5.28 12.59 7.9 12.93 11H18.16L16.58 9.41L18 8L22 12Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_635">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



Mediation.propTypes = {
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

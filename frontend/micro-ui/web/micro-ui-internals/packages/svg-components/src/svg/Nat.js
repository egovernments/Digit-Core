import React from "react";
import PropTypes from "prop-types";

export const Nat = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_2151)">
        <path
          d="M6.82 13H11V11H6.82C6.4 9.84 5.3 9 4 9C2.34 9 1 10.34 1 12C1 13.66 2.34 15 4 15C5.3 15 6.4 14.16 6.82 13ZM4 13C3.45 13 3 12.55 3 12C3 11.45 3.45 11 4 11C4.55 11 5 11.45 5 12C5 12.55 4.55 13 4 13Z"
          fill={fill}
        />
        <path
          d="M23 12L19 9V11H14.95C14.45 5.95 10.19 2 5 2V4C9.42 4 13 7.58 13 12C13 16.42 9.42 20 5 20V22C10.19 22 14.45 18.05 14.95 13H19V15L23 12Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_2151">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



Nat.propTypes = {
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

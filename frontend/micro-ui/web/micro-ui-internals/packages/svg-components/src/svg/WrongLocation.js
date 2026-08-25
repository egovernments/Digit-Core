import React from "react";
import PropTypes from "prop-types";

export const WrongLocation = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11429)">
        <path
          d="M14 10V3.26C13.35 3.09 12.68 3 12 3C7.8 3 4 6.22 4 11.2C4 14.52 6.67 18.45 12 23C17.33 18.45 20 14.52 20 11.2C20 10.79 19.96 10.39 19.91 10H14ZM12 13C10.9 13 10 12.1 10 11C10 9.9 10.9 9 12 9C13.1 9 14 9.9 14 11C14 12.1 13.1 13 12 13Z"
          fill={fill}
        />
        <path
          d="M22.54 2.87996L21.12 1.45996L19 3.58996L16.88 1.45996L15.46 2.87996L17.59 4.99996L15.46 7.11996L16.88 8.53996L19 6.40996L21.12 8.53996L22.54 7.11996L20.41 4.99996L22.54 2.87996Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_11429">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

WrongLocation.propTypes = {
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

import React from "react";
import PropTypes from "prop-types";

export const AddModerator = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_176_582)">
        <path
          d="M13.22 22.61C12.82 22.76 12.42 22.9 12 23C6.84 21.74 3 16.55 3 11V5L12 1L21 5V11C21 11.9 20.89 12.78 20.7 13.65C19.89 13.24 18.97 13 18 13C14.69 13 12 15.69 12 19C12 20.36 12.46 21.61 13.22 22.61ZM19 20V22.99C19 22.99 17.01 23 17 22.99V20H14V18H17V15H19V18H22V20H19Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_176_582">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

AddModerator.propTypes = {
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

import React from "react";
import PropTypes from "prop-types";

export const RuleFolder = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_2471)">
        <path
          d="M20 6H12L10 4H4C2.9 4 2.01 4.9 2.01 6L2 18C2 19.1 2.9 20 4 20H20C21.1 20 22 19.1 22 18V8C22 6.9 21.1 6 20 6ZM7.83 16L5 13.17L6.41 11.76L7.82 13.17L11.36 9.63L12.77 11.04L7.83 16ZM17.41 13L19 14.59L17.59 16L16 14.41L14.41 16L13 14.59L14.59 13L13 11.41L14.41 10L16 11.59L17.59 10L19 11.41L17.41 13Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_2471">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



RuleFolder.propTypes = {
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

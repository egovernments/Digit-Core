import React from "react";
import PropTypes from "prop-types";

export const FactCheck = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_375)">
        <path
          fill-rule="evenodd"
          clip-rule="evenodd"
          d="M20 3H4C2.9 3 2 3.9 2 5V19C2 20.1 2.9 21 4 21H20C21.1 21 22 20.1 22 19V5C22 3.9 21.1 3 20 3ZM10 17H5V15H10V17ZM10 13H5V11H10V13ZM10 9H5V7H10V9ZM14.82 15L12 12.16L13.41 10.75L14.82 12.17L17.99 9L19.41 10.42L14.82 15Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_375">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



FactCheck.propTypes = {
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

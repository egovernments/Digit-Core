import React from "react";
import PropTypes from "prop-types";

export const Rotate360 = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_10784)">
        <path
          d="M12 7C6.48 7 2 9.24 2 12C2 14.24 4.94 16.13 9 16.77V20L13 16L9 12V14.73C5.85 14.17 4 12.83 4 12C4 10.94 7.04 9 12 9C16.96 9 20 10.94 20 12C20 12.73 18.54 13.89 16 14.53V16.58C19.53 15.81 22 14.05 22 12C22 9.24 17.52 7 12 7V7Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_10784">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



Rotate360.propTypes = {
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

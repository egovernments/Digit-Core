import React from "react";
import PropTypes from "prop-types";

export const CloudOff = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_2421)">
        <path
          d="M19.35 10.04C18.67 6.59 15.64 4 12 4C10.52 4 9.15 4.43 7.99 5.17L9.45 6.63C10.21 6.23 11.08 6 12 6C15.04 6 17.5 8.46 17.5 11.5V12H19C20.66 12 22 13.34 22 15C22 16.13 21.36 17.11 20.44 17.62L21.89 19.07C23.16 18.16 24 16.68 24 15C24 12.36 21.95 10.22 19.35 10.04ZM3 5.27L5.75 8.01C2.56 8.15 0 10.77 0 14C0 17.31 2.69 20 6 20H17.73L19.73 22L21 20.73L4.27 4L3 5.27ZM7.73 10L15.73 18H6C3.79 18 2 16.21 2 14C2 11.79 3.79 10 6 10H7.73Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_2421">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



CloudOff.propTypes = {
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

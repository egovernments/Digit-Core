import React from "react";
import PropTypes from "prop-types";

export const GroupAdd = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_176_718)">
        <path
          d="M8 10H5V7H3V10H0V12H3V15H5V12H8V10ZM18 11C19.66 11 20.99 9.66 20.99 8C20.99 6.34 19.66 5 18 5C17.68 5 17.37 5.05 17.09 5.14C17.66 5.95 17.99 6.93 17.99 8C17.99 9.07 17.65 10.04 17.09 10.86C17.37 10.95 17.68 11 18 11ZM13 11C14.66 11 15.99 9.66 15.99 8C15.99 6.34 14.66 5 13 5C11.34 5 10 6.34 10 8C10 9.66 11.34 11 13 11ZM19.62 13.16C20.45 13.89 21 14.82 21 16V18H24V16C24 14.46 21.63 13.51 19.62 13.16ZM13 13C11 13 7 14 7 16V18H19V16C19 14 15 13 13 13Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_176_718">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



GroupAdd.propTypes = {
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

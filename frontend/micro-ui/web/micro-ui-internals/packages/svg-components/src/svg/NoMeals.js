import React from "react";
import PropTypes from "prop-types";

export const NoMeals = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11282)">
        <path
          d="M16.0004 14V6C16.0004 4.24 18.2404 2 21.0004 2V18.17L19.0004 16.17V14H16.0004ZM20.4904 23.31L10.0204 12.85C9.69043 12.94 9.36043 13 9.00043 13V22H7.00043V13C4.79043 13 3.00043 11.21 3.00043 9V5.83L0.69043 3.51L2.10043 2.1L21.9004 21.9L20.4904 23.31ZM6.17043 9L5.00043 7.83V9H6.17043ZM9.00043 2H7.00043V4.17L9.00043 6.17V2ZM13.0004 9V2H11.0004V8.17L12.8504 10.02C12.9404 9.69 13.0004 9.36 13.0004 9Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_11282">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



NoMeals.propTypes = {
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

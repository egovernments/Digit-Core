import React from "react";
import PropTypes from "prop-types";

export const Fireplace = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_176_703)">
        <path
          d="M2 2V22H22V2H2ZM11.86 16.96C12.62 16.72 13.26 15.92 13.39 15.33C13.52 14.77 13.29 14.28 13.19 13.73C13.11 13.27 13.12 12.88 13.27 12.45C13.81 13.66 15.42 14.09 15.25 15.63C15.06 17.33 13.14 18.01 11.86 16.96ZM20 20H18V18H15.98C16.61 17.16 17 16.13 17 15C17 13.11 15.91 12.15 15.15 11.63C12.2 9.61 13 7 13 7C6.27 10.57 6.98 14.47 7 15C7.03 15.96 7.49 17.07 8.23 18H6V20H4V4H20V20Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_176_703">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



Fireplace.propTypes = {
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

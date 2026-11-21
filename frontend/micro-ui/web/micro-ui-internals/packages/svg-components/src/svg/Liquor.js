import React from "react";
import PropTypes from "prop-types";

export const Liquor = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11094)">
        <path d="M3 14C3 15.3 3.84 16.4 5 16.82V20H3V22H9V20H7V16.82C8.16 16.4 9 15.3 9 14V6H3V14ZM5 8H7V11H5V8Z" fill={fill} />
        <path
          d="M20.63 8.54L19.68 8.22C19.28 8.09 19 7.71 19 7.28V3C19 2.45 18.55 2 18 2H15C14.45 2 14 2.45 14 3V7.28C14 7.71 13.72 8.09 13.32 8.23L12.37 8.55C11.55 8.82 11 9.58 11 10.44V20C11 21.1 11.9 22 13 22H20C21.1 22 22 21.1 22 20V10.44C22 9.58 21.45 8.82 20.63 8.54ZM16 4H17V5H16V4ZM13 10.44L13.95 10.12C15.18 9.72 16 8.57 16 7.28V7H17V7.28C17 8.57 17.82 9.72 19.05 10.13L20 10.44V12H13V10.44ZM20 20H13V18H20V20Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_11094">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



Liquor.propTypes = {
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

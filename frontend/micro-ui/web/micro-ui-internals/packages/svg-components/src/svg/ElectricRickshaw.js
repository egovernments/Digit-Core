import React from "react";
import PropTypes from "prop-types";

export const ElectricRickshaw = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11009)">
        <path
          d="M21 11.18V9.72C21 9.25 20.84 8.8 20.54 8.44L16.6 3.72C16.22 3.26 15.66 3 15.06 3H3C1.9 3 1 3.9 1 5V13C1 14.1 1.9 15 3 15H3.18C3.6 16.16 4.7 17 6 17C7.3 17 8.4 16.16 8.82 15H17.19C17.6 16.16 18.7 17 20.01 17C21.67 17 23.01 15.66 23.01 14C23 12.7 22.16 11.6 21 11.18ZM18.4 9H16V6.12L18.4 9ZM3 5H7V9H3V5ZM6 15C5.45 15 5 14.55 5 14C5 13.45 5.45 13 6 13C6.55 13 7 13.45 7 14C7 14.55 6.55 15 6 15ZM9 13V11H12V9H9V5H14V13H9ZM20 15C19.45 15 19 14.55 19 14C19 13.45 19.45 13 20 13C20.55 13 21 13.45 21 14C21 14.55 20.55 15 20 15Z"
          fill={fill}
        />
        <path d="M7 20H11V18L17 21H13V23L7 20Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_1974_11009">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



ElectricRickshaw.propTypes = {
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

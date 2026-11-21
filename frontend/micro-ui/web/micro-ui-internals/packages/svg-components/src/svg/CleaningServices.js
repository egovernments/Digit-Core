import React from "react";
import PropTypes from "prop-types";

export const CleaningServices = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_10894)">
        <path
          d="M16 11H15V3C15 1.9 14.1 1 13 1H11C9.9 1 9 1.9 9 3V11H8C5.24 11 3 13.24 3 16V23H21V16C21 13.24 18.76 11 16 11ZM19 21H17V18C17 17.45 16.55 17 16 17C15.45 17 15 17.45 15 18V21H13V18C13 17.45 12.55 17 12 17C11.45 17 11 17.45 11 18V21H9V18C9 17.45 8.55 17 8 17C7.45 17 7 17.45 7 18V21H5V16C5 14.35 6.35 13 8 13H16C17.65 13 19 14.35 19 16V21Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_10894">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



CleaningServices.propTypes = {
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

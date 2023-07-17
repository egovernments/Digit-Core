import React from "react";
import PropTypes from "prop-types";

export const LocalLaundryService = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11151)">
        <path
          d="M9.17 16.83C10.73 18.39 13.27 18.39 14.83 16.83C16.39 15.27 16.39 12.73 14.83 11.17L9.17 16.83ZM18 2.01L6 2C4.89 2 4 2.89 4 4V20C4 21.11 4.89 22 6 22H18C19.11 22 20 21.11 20 20V4C20 2.89 19.11 2.01 18 2.01ZM10 4C10.55 4 11 4.45 11 5C11 5.55 10.55 6 10 6C9.45 6 9 5.55 9 5C9 4.45 9.45 4 10 4ZM7 4C7.55 4 8 4.45 8 5C8 5.55 7.55 6 7 6C6.45 6 6 5.55 6 5C6 4.45 6.45 4 7 4ZM12 20C8.69 20 6 17.31 6 14C6 10.69 8.69 8 12 8C15.31 8 18 10.69 18 14C18 17.31 15.31 20 12 20Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_11151">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



LocalLaundryService.propTypes = {
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

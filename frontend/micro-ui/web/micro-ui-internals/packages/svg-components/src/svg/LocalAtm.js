import React from "react";
import PropTypes from "prop-types";

export const LocalAtm = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11110)">
        <path
          d="M11 17H13V16H14C14.55 16 15 15.55 15 15V12C15 11.45 14.55 11 14 11H11V10H15V8H13V7H11V8H10C9.45 8 9 8.45 9 9V12C9 12.55 9.45 13 10 13H13V14H9V16H11V17ZM20 4H4C2.89 4 2.01 4.89 2.01 6L2 18C2 19.11 2.89 20 4 20H20C21.11 20 22 19.11 22 18V6C22 4.89 21.11 4 20 4ZM20 18H4V6H20V18Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_11110">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



LocalAtm.propTypes = {
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

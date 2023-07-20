import React from "react";
import PropTypes from "prop-types";

export const NotListedLocation = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11279)">
        <path
          d="M12 2C8.14 2 5 5.14 5 9C5 14.25 12 22 12 22C12 22 19 14.25 19 9C19 5.14 15.86 2 12 2ZM12.88 15.75H11.13V14H12.88V15.75ZM12.88 12.88H11.13C11.13 10.04 13.75 10.26 13.75 8.5C13.75 7.54 12.96 6.75 12 6.75C11.04 6.75 10.25 7.54 10.25 8.5H8.5C8.5 6.57 10.07 5 12 5C13.93 5 15.5 6.57 15.5 8.5C15.5 10.69 12.88 10.91 12.88 12.88V12.88Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_11279">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



NotListedLocation.propTypes = {
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

import React from "react";
import PropTypes from "prop-types";

export const PersonPinCircle = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11304)">
        <path
          d="M12 2C8.14 2 5 5.14 5 9C5 14.25 12 22 12 22C12 22 19 14.25 19 9C19 5.14 15.86 2 12 2ZM12 4C13.1 4 14 4.9 14 6C14 7.11 13.1 8 12 8C10.9 8 10 7.11 10 6C10 4.9 10.9 4 12 4ZM12 14C10.33 14 8.86 13.15 8 11.85C8.02 10.53 10.67 9.8 12 9.8C13.33 9.8 15.98 10.53 16 11.85C15.14 13.15 13.67 14 12 14Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_11304">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



PersonPinCircle.propTypes = {
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

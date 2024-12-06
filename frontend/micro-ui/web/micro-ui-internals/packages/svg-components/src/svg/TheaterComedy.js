import React from "react";
import PropTypes from "prop-types";

export const TheaterComedy = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11388)">
        <path
          d="M2 16.5C2 19.54 4.46 22 7.5 22C10.54 22 13 19.54 13 16.5V10H2V16.5ZM7.5 18.5C6.12 18.5 5 17.83 5 17H10C10 17.83 8.88 18.5 7.5 18.5ZM10 13C10.55 13 11 13.45 11 14C11 14.55 10.55 15 10 15C9.45 15 9 14.55 9 14C9 13.45 9.45 13 10 13ZM5 13C5.55 13 6 13.45 6 14C6 14.55 5.55 15 5 15C4.45 15 4 14.55 4 14C4 13.45 4.45 13 5 13Z"
          fill={fill}
        />
        <path
          d="M11 3V9H14V11.5C14 10.67 15.12 10 16.5 10C17.88 10 19 10.67 19 11.5H14V14V14.39C14.75 14.77 15.6 15 16.5 15C19.54 15 22 12.54 22 9.5V3H11ZM14 8.08C13.45 8.08 13 7.63 13 7.08C13 6.53 13.45 6.08 14 6.08C14.55 6.08 15 6.53 15 7.08C15 7.64 14.55 8.08 14 8.08ZM19 8.08C18.45 8.08 18 7.63 18 7.08C18 6.53 18.45 6.08 19 6.08C19.55 6.08 20 6.53 20 7.08C20 7.64 19.55 8.08 19 8.08Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_11388">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

TheaterComedy.propTypes = {
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

import React from "react";
import PropTypes from "prop-types";

export const AdminPanelSettings = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_51)">
        <path
          d="M17 11C17.34 11 17.67 11.04 18 11.09V6.27L10.5 3L3 6.27V11.18C3 15.72 6.2 19.97 10.5 21C11.05 20.87 11.58 20.68 12.1 20.45C11.41 19.47 11 18.28 11 17C11 13.69 13.69 11 17 11Z"
          fill={fill}
        />
        <path
          d="M17 13C14.79 13 13 14.79 13 17C13 19.21 14.79 21 17 21C19.21 21 21 19.21 21 17C21 14.79 19.21 13 17 13ZM17 14.38C17.62 14.38 18.12 14.89 18.12 15.5C18.12 16.11 17.61 16.62 17 16.62C16.39 16.62 15.88 16.11 15.88 15.5C15.88 14.89 16.38 14.38 17 14.38ZM17 19.75C16.07 19.75 15.26 19.29 14.76 18.58C14.81 17.86 16.27 17.5 17 17.5C17.73 17.5 19.19 17.86 19.24 18.58C18.74 19.29 17.93 19.75 17 19.75Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_51">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

AdminPanelSettings.propTypes = {
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

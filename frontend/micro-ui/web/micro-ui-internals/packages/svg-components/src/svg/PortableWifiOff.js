import React from "react";
import PropTypes from "prop-types";

export const PortableWifiOff = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_2199)">
        <path
          d="M17.56 14.24C17.84 13.55 18 12.79 18 12C18 8.69 15.31 6 12 6C11.21 6 10.45 6.16 9.76 6.44L11.38 8.06C11.58 8.03 11.79 8 12 8C14.21 8 16 9.79 16 12C16 12.21 15.98 12.42 15.95 12.63L17.56 14.24ZM12 4C16.42 4 20 7.58 20 12C20 13.35 19.65 14.62 19.05 15.74L20.52 17.21C21.46 15.69 22 13.91 22 12C22 6.48 17.52 2 12 2C10.09 2 8.31 2.55 6.79 3.47L8.25 4.93C9.37 4.34 10.65 4 12 4ZM3.27 2.5L2 3.77L4.1 5.87C2.79 7.57 2 9.69 2 12C2 15.7 4.01 18.92 6.99 20.65L7.99 18.92C5.61 17.53 4 14.96 4 12C4 10.24 4.57 8.62 5.53 7.31L6.96 8.75C6.36 9.68 6 10.8 6 12C6 14.22 7.21 16.15 9 17.19L10 15.45C8.81 14.75 8 13.48 8 12C8 11.35 8.17 10.75 8.44 10.21L10.02 11.79L10 12C10 13.1 10.9 14 12 14L12.21 13.98L12.22 13.99L19.73 21.5L21 20.23L4.27 3.5L3.27 2.5V2.5Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_2199">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



PortableWifiOff.propTypes = {
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

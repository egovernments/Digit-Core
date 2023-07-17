import React from "react";
import PropTypes from "prop-types";

export const BikeScooter = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_10846)">
        <path
          d="M10 14H10.74L8.82 5.56C8.61 4.65 7.8 4 6.87 4H3V6H6.87L8.29 12.25H8.28C6.12 12.9 4.47 14.73 4.09 17H0V19H6V18C6 15.79 7.79 14 10 14Z"
          fill={fill}
        />
        <path
          d="M19.0004 8H18.1804L16.8304 4.31C16.5504 3.52 15.8004 3 14.9604 3H11.0004V5H14.9604L16.0604 8H10.4004L10.8604 10H15.0004C14.5704 10.58 14.2504 11.25 14.1004 12H11.3104L11.7704 14H14.1004C14.5404 16.23 16.4104 17.88 18.7504 17.99C21.5504 18.12 24.0004 15.8 24.0004 12.99C24.0004 10.2 21.8004 8 19.0004 8ZM19.0004 16C17.3204 16 16.0004 14.68 16.0004 13C16.0004 12.07 16.4104 11.27 17.0504 10.72L18.0104 13.36L19.8904 12.68L18.9204 10.01C18.9504 10.01 18.9804 10 19.0104 10C20.6904 10 22.0104 11.32 22.0104 13C22.0104 14.68 20.6804 16 19.0004 16Z"
          fill={fill}
        />
        <path
          d="M10 15C8.34 15 7 16.34 7 18C7 19.66 8.34 21 10 21C11.66 21 13 19.66 13 18C13 16.34 11.66 15 10 15ZM10 19C9.45 19 9 18.55 9 18C9 17.45 9.45 17 10 17C10.55 17 11 17.45 11 18C11 18.55 10.55 19 10 19Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_10846">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



BikeScooter.propTypes = {
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

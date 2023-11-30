import React from "react";
import PropTypes from "prop-types";

export const TouchApp = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1109)">
        <path
          d="M9 11.24V7.5C9 6.12 10.12 5 11.5 5C12.88 5 14 6.12 14 7.5V11.24C15.21 10.43 16 9.06 16 7.5C16 5.01 13.99 3 11.5 3C9.01 3 7 5.01 7 7.5C7 9.06 7.79 10.43 9 11.24ZM18.84 15.87L14.3 13.61C14.13 13.54 13.95 13.5 13.76 13.5H13V7.5C13 6.67 12.33 6 11.5 6C10.67 6 10 6.67 10 7.5V18.24C6.4 17.48 6.46 17.49 6.33 17.49C6.02 17.49 5.74 17.62 5.54 17.82L4.75 18.62L9.69 23.56C9.96 23.83 10.34 24 10.75 24H17.54C18.29 24 18.87 23.45 18.98 22.72L19.73 17.45C19.74 17.38 19.75 17.31 19.75 17.25C19.75 16.63 19.37 16.09 18.84 15.87Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_1109">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

TouchApp.propTypes = {
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

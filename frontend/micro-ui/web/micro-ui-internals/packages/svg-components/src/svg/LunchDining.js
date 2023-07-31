import React from "react";
import PropTypes from "prop-types";

export const LunchDining = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11203)">
        <path
          fill-rule="evenodd"
          clip-rule="evenodd"
          d="M21.9996 10C22.3196 6.72 17.7197 4 12.0097 4C6.29965 4 1.69965 6.72 2.01965 10H21.9996Z"
          fill={fill}
        />
        <path
          fill-rule="evenodd"
          clip-rule="evenodd"
          d="M5.35 13.5C5.9 13.5 6.13 13.64 6.5 13.86C6.95 14.13 7.57 14.5 8.68 14.5C9.79 14.5 10.41 14.13 10.86 13.86C11.23 13.63 11.45 13.5 12.01 13.5C12.56 13.5 12.79 13.64 13.16 13.86C13.61 14.13 14.23 14.5 15.34 14.5C16.45 14.5 17.07 14.13 17.52 13.86C17.89 13.63 18.11 13.5 18.67 13.5C19.22 13.5 19.45 13.64 19.82 13.86C20.27 14.13 20.89 14.49 21.99 14.5V12.52C21.99 12.52 21.2 12.36 20.83 12.14C20.38 11.87 19.76 11.5 18.65 11.5C17.54 11.5 16.92 11.87 16.47 12.14C16.1 12.37 15.87 12.5 15.32 12.5C14.77 12.5 14.54 12.36 14.17 12.14C13.72 11.87 13.1 11.5 11.99 11.5C10.88 11.5 10.26 11.87 9.81 12.14C9.44 12.37 9.22 12.5 8.66 12.5C8.11 12.5 7.88 12.36 7.51 12.14C7.06 11.87 6.44 11.5 5.33 11.5C4.22 11.5 3.6 11.87 3.15 12.14C2.78 12.37 2.56 12.5 2 12.5V14.5C3.11 14.5 3.73 14.13 4.21 13.86C4.58 13.63 4.8 13.5 5.35 13.5Z"
          fill={fill}
        />
        <path fill-rule="evenodd" clip-rule="evenodd" d="M2 16V18C2 19.1 2.9 20 4 20H20C21.1 20 22 19.1 22 18V16H2Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_1974_11203">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



LunchDining.propTypes = {
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

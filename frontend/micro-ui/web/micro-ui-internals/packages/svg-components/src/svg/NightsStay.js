import React from "react";
import PropTypes from "prop-types";

export const NightsStay = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_176_759)">
        <path
          d="M11.1005 12.08C8.77047 7.57001 10.6005 3.60001 11.6305 2.01001C6.27047 2.20001 1.98047 6.59001 1.98047 12C1.98047 12.14 2.00047 12.28 2.00047 12.42C2.62047 12.15 3.29047 12 4.00047 12C5.66047 12 7.18047 12.83 8.10047 14.15C9.77047 14.63 11.0005 16.17 11.0005 18C11.0005 19.52 10.1305 20.83 8.88047 21.51C9.86047 21.83 10.9105 22.01 11.9905 22.01C15.4905 22.01 18.5705 20.21 20.3605 17.49C18.0005 17.72 13.3805 16.52 11.1005 12.08Z"
          fill={fill}
        />
        <path
          d="M7 16H6.82C6.4 14.84 5.3 14 4 14C2.34 14 1 15.34 1 17C1 18.66 2.34 20 4 20C4.62 20 6.49 20 7 20C8.1 20 9 19.1 9 18C9 16.9 8.1 16 7 16Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_176_759">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



NightsStay.propTypes = {
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

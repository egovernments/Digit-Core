import React from "react";
import PropTypes from "prop-types";

export const Unpublished = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1140)">
        <path
          d="M21.1896 21.19L2.80965 2.81L1.38965 4.22L3.65965 6.49C2.60965 8.07 1.99965 9.96 1.99965 12C1.99965 17.52 6.47965 22 11.9996 22C14.0396 22 15.9296 21.39 17.5096 20.34L19.7796 22.61L21.1896 21.19ZM10.5896 16.6L6.34965 12.36L7.75965 10.95L10.5896 13.78L10.7696 13.6L12.1796 15.01L10.5896 16.6ZM13.5896 10.76L6.48965 3.66C8.06965 2.61 9.95965 2 11.9996 2C17.5196 2 21.9996 6.48 21.9996 12C21.9996 14.04 21.3896 15.93 20.3396 17.51L14.9996 12.17L17.6496 9.52L16.2396 8.11L13.5896 10.76Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_1140">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

Unpublished.propTypes = {
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

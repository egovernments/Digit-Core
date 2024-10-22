import React from "react";
import PropTypes from "prop-types";

export const TransferWithinStation = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11404)">
        <path
          d="M16.49 15.5V13.75L14 16.25L16.49 18.75V17H22V15.5H16.49ZM19.51 19.75H14V21.25H19.51V23L22 20.5L19.51 18V19.75ZM9.5 5.5C10.6 5.5 11.5 4.6 11.5 3.5C11.5 2.4 10.6 1.5 9.5 1.5C8.4 1.5 7.5 2.4 7.5 3.5C7.5 4.6 8.4 5.5 9.5 5.5ZM5.75 8.9L3 23H5.1L6.85 15L9 17V23H11V15.45L8.95 13.4L9.55 10.4C10.85 12 12.8 13 15 13V11C13.15 11 11.55 10 10.65 8.55L9.7 6.95C9.35 6.35 8.7 6 8 6C7.75 6 7.5 6.05 7.25 6.15L2 8.3V13H4V9.65L5.75 8.9Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_11404">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

TransferWithinStation.propTypes = {
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

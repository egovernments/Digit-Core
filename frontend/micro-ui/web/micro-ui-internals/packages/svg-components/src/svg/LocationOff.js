import React from "react";
import PropTypes from "prop-types";

export const LocationOff = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_2112)">
        <path
          d="M12 6.5C13.38 6.5 14.5 7.62 14.5 9C14.5 9.74 14.17 10.39 13.67 10.85L17.3 14.48C18.28 12.62 19 10.68 19 9C19 5.13 15.87 2 12 2C10.02 2 8.24 2.83 6.96 4.15L10.15 7.34C10.61 6.82 11.26 6.5 12 6.5ZM16.37 16.1L11.74 11.47L11.63 11.36L3.27 3L2 4.27L5.18 7.45C5.07 7.95 5 8.47 5 9C5 14.25 12 22 12 22C12 22 13.67 20.15 15.38 17.65L18.73 21L20 19.73L16.37 16.1Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_2112">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



LocationOff.propTypes = {
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

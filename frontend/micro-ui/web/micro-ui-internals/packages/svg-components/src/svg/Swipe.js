import React from "react";
import PropTypes from "prop-types";

export const Swipe = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1033)">
        <path
          d="M19.75 16.25C19.75 16.31 19.74 16.38 19.73 16.45L18.98 21.72C18.87 22.45 18.29 23 17.54 23H10.75C10.34 23 9.96 22.83 9.69 22.56L4.75 17.62L5.54 16.82C5.74 16.62 6.02 16.49 6.33 16.49C6.42 16.49 6.49 16.51 6.57 16.52L10 17.24V6.5C10 5.67 10.67 5 11.5 5C12.33 5 13 5.67 13 6.5V12.5H13.76C13.95 12.5 14.13 12.54 14.3 12.61L18.84 14.87C19.37 15.09 19.75 15.63 19.75 16.25ZM20.13 3.87C18.69 2.17 15.6 1 12 1C8.4 1 5.31 2.17 3.87 3.87L2 2V7H7L4.93 4.93C5.93 3.64 8.63 2.5 12 2.5C15.37 2.5 18.07 3.64 19.07 4.93L17 7H22V2L20.13 3.87V3.87Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_1033">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

Swipe.propTypes = {
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

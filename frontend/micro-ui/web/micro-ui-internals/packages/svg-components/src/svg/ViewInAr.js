import React from "react";
import PropTypes from "prop-types";

export const ViewInAr = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1183)">
        <path
          d="M18.25 7.6L12.75 4.42C12.29 4.15 11.71 4.15 11.25 4.42L5.75 7.6C5.29 7.87 5 8.36 5 8.9V15.25C5 15.79 5.29 16.28 5.75 16.55L11.25 19.73C11.71 20 12.29 20 12.75 19.73L18.25 16.55C18.71 16.28 19 15.79 19 15.25V8.9C19 8.36 18.71 7.87 18.25 7.6V7.6ZM7 14.96V10.34L11 12.66V17.27L7 14.96V14.96ZM12 10.93L8 8.61L12 6.3L16 8.61L12 10.93ZM13 17.27V12.66L17 10.34V14.96L13 17.27ZM7 2H3.5C2.67 2 2 2.67 2 3.5V7H4V4H7V2ZM17 2H20.5C21.33 2 22 2.67 22 3.5V7H20V4H17V2ZM7 22H3.5C2.67 22 2 21.33 2 20.5V17H4V20H7V22ZM17 22H20.5C21.33 22 22 21.33 22 20.5V17H20V20H17V22Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_1183">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

ViewInAr.propTypes = {
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

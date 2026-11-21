import React from "react";
import PropTypes from "prop-types";

export const LocalSee = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11190)">
        <path
          d="M11.9998 15.1998C13.7671 15.1998 15.1998 13.7671 15.1998 11.9998C15.1998 10.2325 13.7671 8.7998 11.9998 8.7998C10.2325 8.7998 8.7998 10.2325 8.7998 11.9998C8.7998 13.7671 10.2325 15.1998 11.9998 15.1998Z"
          fill={fill}
        />
        <path
          d="M9 2L7.17 4H4C2.9 4 2 4.9 2 6V18C2 19.1 2.9 20 4 20H20C21.1 20 22 19.1 22 18V6C22 4.9 21.1 4 20 4H16.83L15 2H9ZM12 17C9.24 17 7 14.76 7 12C7 9.24 9.24 7 12 7C14.76 7 17 9.24 17 12C17 14.76 14.76 17 12 17Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_11190">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



LocalSee.propTypes = {
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

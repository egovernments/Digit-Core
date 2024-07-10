import React from "react";
import PropTypes from "prop-types";

export const PestControlRodent = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11317)">
        <path
          d="M21.31 17.38L18.92 15.25C19.44 12.89 17.56 11 15.5 11C14.34 11 12 11.9 12 14.5C12 15.47 12.39 16.34 13.03 16.97L12.32 17.68C11.5 16.87 11 15.74 11 14.5C11 12.8 11.96 11.33 13.35 10.57C12.65 10.21 11.87 10 11.07 10C8.69 10 6.7 11.65 6.16 13.87C4.91 13.5 4 12.36 4 11C4 9.34 5.34 8 7 8C7.94 8 8.56 8 9.5 8C10.88 8 12 6.88 12 5.5C12 4.12 10.88 3 9.5 3H8C7.45 3 7 3.45 7 4C7 4.55 7.45 5 8 5H9.5C9.78 5 10 5.22 10 5.5C10 5.78 9.78 6 9.5 6C9.47 6 9 6 7 6C4.24 6 2 8.24 2 11C2 13.42 3.72 15.44 6 15.9V15.93C6 18.73 8.27 21 11.07 21H19.93C21.8 21 22.74 18.66 21.31 17.38ZM18 19C17.45 19 17 18.55 17 18C17 17.45 17.45 17 18 17C18.55 17 19 17.45 19 18C19 18.55 18.55 19 18 19Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_11317">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



PestControlRodent.propTypes = {
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

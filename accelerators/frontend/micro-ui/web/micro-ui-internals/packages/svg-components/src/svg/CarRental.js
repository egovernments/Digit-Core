import React from "react";
import PropTypes from "prop-types";

export const CarRental = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_10867)">
        <path
          d="M16.39 9H7.61C7.18 9 6.8 9.28 6.66 9.68L5 14.68V21.49C5 21.78 5.23 22 5.5 22H6.5C6.78 22 7 21.78 7 21.5V20H17V21.5C17 21.78 17.22 22 17.5 22H18.5C18.78 22 19 21.78 19 21.5V14.69L17.34 9.69C17.2 9.28 16.82 9 16.39 9ZM7.78 18C7.1 18 6.56 17.46 6.56 16.78C6.56 16.1 7.1 15.56 7.78 15.56C8.46 15.56 9 16.11 9 16.78C9 17.45 8.46 18 7.78 18ZM16.22 18C15.55 18 15 17.46 15 16.78C15 16.1 15.54 15.56 16.22 15.56C16.9 15.56 17.44 16.1 17.44 16.78C17.44 17.46 16.9 18 16.22 18ZM6.29 14L7.62 10H16.4L17.73 14H6.29Z"
          fill={fill}
        />
        <path
          d="M10.83 3C10.41 1.83 9.3 1 8 1C6.34 1 5 2.34 5 4C5 5.65 6.34 7 8 7C9.3 7 10.41 6.16 10.83 5H16V7H18V5H19V3H10.83ZM8 5C7.45 5 7 4.55 7 4C7 3.45 7.45 3 8 3C8.55 3 9 3.45 9 4C9 4.55 8.55 5 8 5Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_10867">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



CarRental.propTypes = {
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

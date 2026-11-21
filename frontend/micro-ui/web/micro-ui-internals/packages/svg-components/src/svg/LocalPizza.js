import React from "react";
import PropTypes from "prop-types";

export const LocalPizza = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11175)">
        <path
          d="M11.9998 2C8.42977 2 5.22977 3.54 3.00977 6L11.9998 22L20.9898 6C18.7798 3.55 15.5698 2 11.9998 2ZM6.99977 7C6.99977 5.9 7.89977 5 8.99977 5C10.0998 5 10.9998 5.9 10.9998 7C10.9998 8.1 10.0998 9 8.99977 9C7.89977 9 6.99977 8.1 6.99977 7ZM11.9998 15C10.8998 15 9.99977 14.1 9.99977 13C9.99977 11.9 10.8998 11 11.9998 11C13.0998 11 13.9998 11.9 13.9998 13C13.9998 14.1 13.0998 15 11.9998 15Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_11175">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



LocalPizza.propTypes = {
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

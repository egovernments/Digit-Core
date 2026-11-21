import React from "react";
import PropTypes from "prop-types";

export const LockClock = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_602)">
        <path
          d="M14.5 14.2L17.4 15.9L16.6 17.2L13 15V10H14.5V14.2ZM22 14C22 18.41 18.41 22 14 22C11.98 22 10.14 21.24 8.73 20H4C2.85 20 2 19.15 2 18V9C2 7.88 2.89 7.04 4 7V6.5C4 4.01 6.01 2 8.5 2C10.84 2 12.74 3.79 12.96 6.08C13.3 6.03 13.65 6 14 6C18.41 6 22 9.59 22 14ZM6 7H11V6.26C10.88 4.99 9.8 4 8.5 4C7.12 4 6 5.12 6 6.5V7ZM20 14C20 10.69 17.31 8 14 8C10.69 8 8 10.69 8 14C8 17.31 10.69 20 14 20C17.31 20 20 17.31 20 14Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_602">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



LockClock.propTypes = {
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

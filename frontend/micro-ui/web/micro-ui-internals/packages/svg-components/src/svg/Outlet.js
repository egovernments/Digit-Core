import React from "react";
import PropTypes from "prop-types";

export const Outlet = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_706)">
        <path
          d="M12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 12 2ZM9 12C8.45 12 8 11.55 8 11V8C8 7.45 8.45 7 9 7C9.55 7 10 7.45 10 8V11C10 11.55 9.55 12 9 12ZM14 18H10V16C10 14.9 10.9 14 12 14C13.1 14 14 14.9 14 16V18ZM16 11C16 11.55 15.55 12 15 12C14.45 12 14 11.55 14 11V8C14 7.45 14.45 7 15 7C15.55 7 16 7.45 16 8V11Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_706">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



Outlet.propTypes = {
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

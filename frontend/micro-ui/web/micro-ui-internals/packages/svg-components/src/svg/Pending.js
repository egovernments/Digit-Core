import React from "react";
import PropTypes from "prop-types";

export const Pending = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_722)">
        <path
          d="M12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 12 2ZM7 13.5C6.17 13.5 5.5 12.83 5.5 12C5.5 11.17 6.17 10.5 7 10.5C7.83 10.5 8.5 11.17 8.5 12C8.5 12.83 7.83 13.5 7 13.5ZM12 13.5C11.17 13.5 10.5 12.83 10.5 12C10.5 11.17 11.17 10.5 12 10.5C12.83 10.5 13.5 11.17 13.5 12C13.5 12.83 12.83 13.5 12 13.5ZM17 13.5C16.17 13.5 15.5 12.83 15.5 12C15.5 11.17 16.17 10.5 17 10.5C17.83 10.5 18.5 11.17 18.5 12C18.5 12.83 17.83 13.5 17 13.5Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_722">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



Pending.propTypes = {
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

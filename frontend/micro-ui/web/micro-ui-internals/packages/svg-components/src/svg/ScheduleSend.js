import React from "react";
import PropTypes from "prop-types";

export const ScheduleSend = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_883)">
        <path
          d="M16.5 12.5H15V16.5L18 18.5L18.75 17.27L16.5 15.75V12.5ZM16 9L2 3V10L11 12L2 14V21L9.27 17.89C10.09 20.83 12.79 23 16 23C19.86 23 23 19.86 23 16C23 12.14 19.86 9 16 9ZM16 21C13.25 21 11.02 18.78 11 16.03V15.96C11.02 13.22 13.25 10.99 16 10.99C18.76 10.99 21 13.23 21 15.99C21 18.75 18.76 21 16 21Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_883">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



ScheduleSend.propTypes = {
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

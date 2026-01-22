import React from "react";
import PropTypes from "prop-types";

export const MarkAsUnread = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_629)">
        <path
          d="M18.83 7H16.23L10.5 4L4 7.4V17C2.9 17 2 16.1 2 15V7.17C2 6.64 2.32 6.08 2.8 5.83L10.5 2L18.04 5.83C18.47 6.06 18.77 6.53 18.83 7ZM20 8H7C5.9 8 5 8.9 5 10V19C5 20.1 5.9 21 7 21H20C21.1 21 22 20.1 22 19V10C22 8.9 21.1 8 20 8ZM20 11.67L13.5 15L7 11.67V10L13.5 13.33L20 10V11.67Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_629">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



MarkAsUnread.propTypes = {
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

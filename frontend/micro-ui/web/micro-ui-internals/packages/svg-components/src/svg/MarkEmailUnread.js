import React from "react";
import PropTypes from "prop-types";

export const MarkEmailUnread = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_2133)">
        <path
          d="M22 8.98V18C22 19.1 21.1 20 20 20H4C2.9 20 2 19.1 2 18V6C2 4.9 2.9 4 4 4H14.1C14.04 4.32 14 4.66 14 5C14 6.48 14.65 7.79 15.67 8.71L12 11L4 6V8L12 13L17.3 9.68C17.84 9.88 18.4 10 19 10C20.13 10 21.16 9.61 22 8.98ZM16 5C16 6.66 17.34 8 19 8C20.66 8 22 6.66 22 5C22 3.34 20.66 2 19 2C17.34 2 16 3.34 16 5Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_2133">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



MarkEmailUnread.propTypes = {
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

import React from "react";
import PropTypes from "prop-types";

export const MarkChatUnread = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_2125)">
        <path
          d="M22 6.98V16C22 17.1 21.1 18 20 18H6L2 22V4C2 2.9 2.9 2 4 2H14.1C14.04 2.32 14 2.66 14 3C14 5.76 16.24 8 19 8C20.13 8 21.16 7.61 22 6.98ZM16 3C16 4.66 17.34 6 19 6C20.66 6 22 4.66 22 3C22 1.34 20.66 0 19 0C17.34 0 16 1.34 16 3Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_2125">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



MarkChatUnread.propTypes = {
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

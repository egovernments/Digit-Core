import React from "react";
import PropTypes from "prop-types";

export const MarkChatRead = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_2121)">
        <path
          d="M17.34 20L13.8 16.46L15.21 15.05L17.33 17.17L21.57 12.93L23 14.34L17.34 20ZM12 17C12 13.13 15.13 10 19 10C20.08 10 21.09 10.25 22 10.68V4C22 2.9 21.1 2 20 2H4C2.9 2 2 2.9 2 4V22L6 18H12C12 17.83 12.01 17.67 12.03 17.5C12.01 17.34 12 17.17 12 17Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_2121">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



MarkChatRead.propTypes = {
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

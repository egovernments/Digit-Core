import React from "react";
import PropTypes from "prop-types";

export const EmojiFlags = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_176_636)">
        <path
          d="M14 9L13 7H7V5.72C7.6 5.38 8 4.74 8 4C8 2.9 7.1 2 6 2C4.9 2 4 2.9 4 4C4 4.74 4.4 5.38 5 5.72V21H7V17H12L13 19H20V9H14ZM18 17H14L13 15H7V9H12L13 11H18V17Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_176_636">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



EmojiFlags.propTypes = {
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

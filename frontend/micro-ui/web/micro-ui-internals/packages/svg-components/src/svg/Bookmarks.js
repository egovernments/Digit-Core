import React from "react";
import PropTypes from "prop-types";

export const Bookmarks = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_158)">
        <path
          d="M19 18L21 19V3C21 1.9 20.1 1 19 1H8.99C7.89 1 7 1.9 7 3H17C18.1 3 19 3.9 19 5V18ZM15 5H5C3.9 5 3 5.9 3 7V23L10 20L17 23V7C17 5.9 16.1 5 15 5Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_158">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



Bookmarks.propTypes = {
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

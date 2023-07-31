import React from "react";
import PropTypes from "prop-types";

export const StickyNotesSecondary = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_994)">
        <path
          d="M19 3H4.99C3.89 3 3 3.9 3 5L3.01 19C3.01 20.1 3.9 21 5 21H15L21 15V5C21 3.9 20.1 3 19 3ZM7 8H17V10H7V8ZM12 14H7V12H12V14ZM14 19.5V14H19.5L14 19.5Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_994">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



StickyNotesSecondary.propTypes = {
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

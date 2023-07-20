import React from "react";
import PropTypes from "prop-types";

export const Pages = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_176_793)">
        <path
          d="M3 5V11H8L7 7L11 8V3H5C3.9 3 3 3.9 3 5ZM8 13H3V19C3 20.1 3.9 21 5 21H11V16L7 17L8 13ZM17 17L13 16V21H19C20.1 21 21 20.1 21 19V13H16L17 17ZM19 3H13V8L17 7L16 11H21V5C21 3.9 20.1 3 19 3Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_176_793">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



Pages.propTypes = {
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

import React from "react";
import PropTypes from "prop-types";

export const Lightbulb = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_572)">
        <path
          d="M9 21C9 21.5 9.4 22 10 22H14C14.6 22 15 21.5 15 21V20H9V21ZM12 2C8.1 2 5 5.1 5 9C5 11.4 6.2 13.5 8 14.7V17C8 17.5 8.4 18 9 18H15C15.6 18 16 17.5 16 17V14.7C17.8 13.4 19 11.3 19 9C19 5.1 15.9 2 12 2Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_572">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



Lightbulb.propTypes = {
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

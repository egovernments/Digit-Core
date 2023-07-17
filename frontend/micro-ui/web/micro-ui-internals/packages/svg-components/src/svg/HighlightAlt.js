import React from "react";
import PropTypes from "prop-types";

export const HighlightAlt = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_483)">
        <path
          d="M17 5H15V3H17V5ZM15 21H17V18.41L19.59 21L21 19.59L18.41 17H21V15H15V21ZM19 9H21V7H19V9ZM19 13H21V11H19V13ZM11 21H13V19H11V21ZM7 5H9V3H7V5ZM3 17H5V15H3V17ZM5 21V19H3C3 20.1 3.9 21 5 21ZM19 3V5H21C21 3.9 20.1 3 19 3ZM11 5H13V3H11V5ZM3 9H5V7H3V9ZM7 21H9V19H7V21ZM3 13H5V11H3V13ZM3 5H5V3C3.9 3 3 3.9 3 5Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_483">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



HighlightAlt.propTypes = {
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

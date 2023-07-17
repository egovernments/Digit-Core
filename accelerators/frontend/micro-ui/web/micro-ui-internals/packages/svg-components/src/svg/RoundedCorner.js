import React from "react";
import PropTypes from "prop-types";

export const RoundedCorner = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_858)">
        <path
          d="M19 19H21V21H19V19ZM19 17H21V15H19V17ZM3 13H5V11H3V13ZM3 17H5V15H3V17ZM3 9H5V7H3V9ZM3 5H5V3H3V5ZM7 5H9V3H7V5ZM15 21H17V19H15V21ZM11 21H13V19H11V21ZM15 21H17V19H15V21ZM7 21H9V19H7V21ZM3 21H5V19H3V21ZM21 8C21 5.24 18.76 3 16 3H11V5H16C17.65 5 19 6.35 19 8V13H21V8Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_858">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



RoundedCorner.propTypes = {
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

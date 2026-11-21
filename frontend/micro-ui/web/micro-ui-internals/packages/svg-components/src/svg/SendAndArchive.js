import React from "react";
import PropTypes from "prop-types";

export const SendAndArchive = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_899)">
        <path
          d="M21 10H18L2 3V10L11 12L2 14V21L10 17.5V21C10 22.1 10.9 23 12 23H21C22.1 23 23 22.1 23 21V12C23 10.9 22.1 10 21 10ZM21 21H12V12H21V21ZM16.5 20L13 16H15V13H18V16H20L16.5 20Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_899">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



SendAndArchive.propTypes = {
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

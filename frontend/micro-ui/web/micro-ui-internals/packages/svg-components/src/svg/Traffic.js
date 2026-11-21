import React from "react";
import PropTypes from "prop-types";

export const Traffic = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11395)">
        <path
          d="M20 10H17V8.86C18.72 8.41 20 6.86 20 5H17V4C17 3.45 16.55 3 16 3H8C7.45 3 7 3.45 7 4V5H4C4 6.86 5.28 8.41 7 8.86V10H4C4 11.86 5.28 13.41 7 13.86V15H4C4 16.86 5.28 18.41 7 18.86V20C7 20.55 7.45 21 8 21H16C16.55 21 17 20.55 17 20V18.86C18.72 18.41 20 16.86 20 15H17V13.86C18.72 13.41 20 11.86 20 10ZM12 19C10.89 19 10 18.1 10 17C10 15.9 10.89 15 12 15C13.1 15 14 15.9 14 17C14 18.1 13.11 19 12 19ZM12 14C10.89 14 10 13.1 10 12C10 10.9 10.89 10 12 10C13.1 10 14 10.9 14 12C14 13.1 13.11 14 12 14ZM12 9C10.89 9 10 8.1 10 7C10 5.89 10.89 5 12 5C13.1 5 14 5.89 14 7C14 8.1 13.11 9 12 9Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_11395">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

Traffic.propTypes = {
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

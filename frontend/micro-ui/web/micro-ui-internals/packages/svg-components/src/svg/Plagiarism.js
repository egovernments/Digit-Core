import React from "react";
import PropTypes from "prop-types";

export const Plagiarism = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_768)">
        <path
          d="M14 2H6C4.9 2 4 2.9 4 4V20C4 21.1 4.89 22 5.99 22H18C19.1 22 20 21.1 20 20V8L14 2ZM15.04 19.45L13.16 17.57C11.83 18.28 10.15 18.1 9.03 16.98C7.66 15.61 7.66 13.4 9.03 12.03C10.4 10.66 12.61 10.66 13.98 12.03C15.1 13.15 15.29 14.83 14.57 16.16L16.45 18.04L15.04 19.45ZM13 9V3.5L18.5 9H13Z"
          fill={fill}
        />
        <path
          d="M11.5 16C12.3284 16 13 15.3284 13 14.5C13 13.6716 12.3284 13 11.5 13C10.6716 13 10 13.6716 10 14.5C10 15.3284 10.6716 16 11.5 16Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_768">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



Plagiarism.propTypes = {
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

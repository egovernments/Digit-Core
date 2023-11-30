import React from "react";
import PropTypes from "prop-types";

export const PersonSearch = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_2170)">
        <path d="M10 12C12.2091 12 14 10.2091 14 8C14 5.79086 12.2091 4 10 4C7.79086 4 6 5.79086 6 8C6 10.2091 7.79086 12 10 12Z" fill={fill} />
        <path d="M10.35 14.01C7.62 13.91 2 15.27 2 18V20H11.54C9.07 17.24 10.31 14.11 10.35 14.01Z" fill={fill} />
        <path
          d="M19.43 18.02C19.79 17.43 20 16.74 20 16C20 13.79 18.21 12 16 12C13.79 12 12 13.79 12 16C12 18.21 13.79 20 16 20C16.74 20 17.43 19.78 18.02 19.43L20.59 22L22 20.59L19.43 18.02ZM16 18C14.9 18 14 17.1 14 16C14 14.9 14.9 14 16 14C17.1 14 18 14.9 18 16C18 17.1 17.1 18 16 18Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_2170">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



PersonSearch.propTypes = {
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

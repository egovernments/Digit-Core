import React from "react";
import PropTypes from "prop-types";

export const Hvac = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11068)">
        <path d="M11.9999 16C13.0099 16 13.9099 15.61 14.6199 15H9.37988C10.0899 15.61 10.9899 16 11.9999 16Z" fill={fill} />
        <path d="M8.55957 14H15.4496C15.7096 13.55 15.8896 13.04 15.9596 12.5H8.05957C8.11957 13.04 8.28957 13.55 8.55957 14Z" fill={fill} />
        <path d="M11.9999 8C10.9899 8 10.0899 8.39 9.37988 9H14.6199C13.9099 8.39 13.0099 8 11.9999 8Z" fill={fill} />
        <path d="M8.5598 10C8.2998 10.45 8.1198 10.96 8.0498 11.5H15.9498C15.8798 10.96 15.7098 10.45 15.4398 10H8.5598Z" fill={fill} />
        <path
          d="M19 3H5C3.9 3 3 3.9 3 5V19C3 20.1 3.9 21 5 21H19C20.1 21 21 20.1 21 19V5C21 3.9 20.1 3 19 3ZM12 18C8.69 18 6 15.31 6 12C6 8.69 8.69 6 12 6C15.31 6 18 8.69 18 12C18 15.31 15.31 18 12 18Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_11068">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



Hvac.propTypes = {
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

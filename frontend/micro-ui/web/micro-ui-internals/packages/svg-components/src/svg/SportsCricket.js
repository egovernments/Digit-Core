import React from "react";
import PropTypes from "prop-types";

export const SportsCricket = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_176_972)">
        <path
          d="M15.0498 12.8101L6.55984 4.32009C6.16984 3.93009 5.53984 3.93009 5.14984 4.32009L2.31984 7.15009C1.92984 7.54009 1.92984 8.17009 2.31984 8.56009L10.8098 17.0501C11.1998 17.4401 11.8298 17.4401 12.2198 17.0501L15.0498 14.2201C15.4398 13.8301 15.4398 13.2001 15.0498 12.8101Z"
          fill={fill}
        />
        <path d="M15.755 16.3421L14.3408 17.7563L18.5834 21.9989L19.9976 20.5847L15.755 16.3421Z" fill={fill} />
        <path d="M18.5 9C20.433 9 22 7.433 22 5.5C22 3.567 20.433 2 18.5 2C16.567 2 15 3.567 15 5.5C15 7.433 16.567 9 18.5 9Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_176_972">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



SportsCricket.propTypes = {
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

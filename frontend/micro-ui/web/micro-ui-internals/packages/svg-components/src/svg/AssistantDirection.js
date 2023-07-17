import React from "react";
import PropTypes from "prop-types";

export const AssistantDirection = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1488)">
        <path
          d="M14 10H9C8.4 10 8 10.4 8 11V15H10V12H14V14.5L17.5 11L14 7.5V10ZM12 1C5.9 1 1 5.9 1 12C1 18.1 5.9 23 12 23C18.1 23 23 18.1 23 12C23 5.9 18.1 1 12 1ZM19.73 12.58L12.54 19.8C12.19 20.07 11.75 20.07 11.39 19.8L4.2 12.58C3.93 12.22 3.93 11.78 4.2 11.42L11.39 4.2C11.74 3.93 12.18 3.93 12.54 4.2L19.73 11.42C20.09 11.69 20.09 12.22 19.73 12.58Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_1488">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

AssistantDirection.propTypes = {
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

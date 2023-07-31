import React from "react";
import PropTypes from "prop-types";

export const RemoveModerator = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_176_873)">
        <path
          d="M22.27 21.73L18.73 18.18L5.78 5.23L2.27 1.72L1 2.99L3.01 5H3V11C3 16.55 6.84 21.74 12 23C14.16 22.47 16.08 21.24 17.6 19.59L21 23L22.27 21.73ZM13 9.92L19.67 16.59C20.51 14.87 21 12.96 21 11V5L12 1L6.52 3.44L11 7.92L13 9.92Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_176_873">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



RemoveModerator.propTypes = {
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

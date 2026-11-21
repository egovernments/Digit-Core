import React from "react";
import PropTypes from "prop-types";

export const MilitaryTech = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_176_748)">
        <path
          d="M17 10.43V2H7V10.43C7 10.78 7.18 11.11 7.49 11.29L11.67 13.8L10.68 16.14L7.27 16.43L9.86 18.67L9.07 22L12 20.23L14.93 22L14.15 18.67L16.74 16.43L13.33 16.14L12.34 13.8L16.52 11.29C16.82 11.11 17 10.79 17 10.43ZM13 12.23L12 12.83L11 12.23V3H13V12.23Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_176_748">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



MilitaryTech.propTypes = {
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

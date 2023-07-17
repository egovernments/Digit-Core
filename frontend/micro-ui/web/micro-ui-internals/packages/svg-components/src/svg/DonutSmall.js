import React from "react";
import PropTypes from "prop-types";

export const DonutSmall = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_321)">
        <path
          d="M11 9.16V2C6 2.5 2 6.79 2 12C2 17.21 6 21.5 11 22V14.84C10 14.43 9 13.32 9 12C9 10.68 10 9.57 11 9.16ZM14.86 11H22C21.52 6.25 18 2.47 13 2V9.16C14 9.46 14.52 10.14 14.86 11ZM13 14.84V22C18 21.53 21.52 17.75 22 13H14.86C14.52 13.86 14 14.54 13 14.84Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_321">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



DonutSmall.propTypes = {
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

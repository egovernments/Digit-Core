import React from "react";
import PropTypes from "prop-types";

export const SettingsInputComponent = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_927)">
        <path
          d="M5 2C5 1.45 4.55 1 4 1C3.45 1 3 1.45 3 2V6H1V12H7V6H5V2ZM9 16C9 17.3 9.84 18.4 11 18.82V23H13V18.82C14.16 18.41 15 17.31 15 16V14H9V16ZM1 16C1 17.3 1.84 18.4 3 18.82V23H5V18.82C6.16 18.4 7 17.3 7 16V14H1V16ZM21 6V2C21 1.45 20.55 1 20 1C19.45 1 19 1.45 19 2V6H17V12H23V6H21ZM13 2C13 1.45 12.55 1 12 1C11.45 1 11 1.45 11 2V6H9V12H15V6H13V2ZM17 16C17 17.3 17.84 18.4 19 18.82V23H21V18.82C22.16 18.41 23 17.31 23 16V14H17V16Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_927">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



SettingsInputComponent.propTypes = {
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

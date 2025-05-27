import React from "react";
import PropTypes from "prop-types";

export const SettingsInputVideo = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_936)">
        <path
          d="M8 11.5C8 10.67 7.33 10 6.5 10C5.67 10 5 10.67 5 11.5C5 12.33 5.67 13 6.5 13C7.33 13 8 12.33 8 11.5ZM15 6.5C15 5.67 14.33 5 13.5 5H10.5C9.67 5 9 5.67 9 6.5C9 7.33 9.67 8 10.5 8H13.5C14.33 8 15 7.33 15 6.5ZM8.5 15C7.67 15 7 15.67 7 16.5C7 17.33 7.67 18 8.5 18C9.33 18 10 17.33 10 16.5C10 15.67 9.33 15 8.5 15ZM12 1C5.93 1 1 5.93 1 12C1 18.07 5.93 23 12 23C18.07 23 23 18.07 23 12C23 5.93 18.07 1 12 1ZM12 21C7.04 21 3 16.96 3 12C3 7.04 7.04 3 12 3C16.96 3 21 7.04 21 12C21 16.96 16.96 21 12 21ZM17.5 10C16.67 10 16 10.67 16 11.5C16 12.33 16.67 13 17.5 13C18.33 13 19 12.33 19 11.5C19 10.67 18.33 10 17.5 10ZM15.5 15C14.67 15 14 15.67 14 16.5C14 17.33 14.67 18 15.5 18C16.33 18 17 17.33 17 16.5C17 15.67 16.33 15 15.5 15Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_936">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



SettingsInputVideo.propTypes = {
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

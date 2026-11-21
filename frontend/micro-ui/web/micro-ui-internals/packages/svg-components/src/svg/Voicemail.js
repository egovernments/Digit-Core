import React from "react";
import PropTypes from "prop-types";

export const Voicemail = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_2283)">
        <path
          d="M18.5 6C15.46 6 13 8.46 13 11.5C13 12.83 13.47 14.05 14.26 15H9.74C10.53 14.05 11 12.83 11 11.5C11 8.46 8.54 6 5.5 6C2.46 6 0 8.46 0 11.5C0 14.54 2.46 17 5.5 17H18.5C21.54 17 24 14.54 24 11.5C24 8.46 21.54 6 18.5 6ZM5.5 15C3.57 15 2 13.43 2 11.5C2 9.57 3.57 8 5.5 8C7.43 8 9 9.57 9 11.5C9 13.43 7.43 15 5.5 15ZM18.5 15C16.57 15 15 13.43 15 11.5C15 9.57 16.57 8 18.5 8C20.43 8 22 9.57 22 11.5C22 13.43 20.43 15 18.5 15Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_2283">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

Voicemail.propTypes = {
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

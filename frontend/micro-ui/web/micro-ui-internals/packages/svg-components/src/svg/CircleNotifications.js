import React from "react";
import PropTypes from "prop-types";

export const CircleNotifications = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_221)">
        <path
          d="M12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 12 2ZM12 18.5C11.17 18.5 10.5 17.83 10.5 17H13.5C13.5 17.83 12.83 18.5 12 18.5ZM17 16H7V15L8 14V11.39C8 9.27 9.03 7.47 11 7V6.5C11 5.93 11.43 5.5 12 5.5C12.57 5.5 13 5.93 13 6.5V7C14.97 7.47 16 9.28 16 11.39V14L17 15V16Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_221">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



CircleNotifications.propTypes = {
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

import React from "react";
import PropTypes from "prop-types";

export const PhonelinkErase = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_2181)">
        <path
          d="M13 8.2L12 7.2L8 11.2L4 7.2L3 8.2L7 12.2L3 16.2L4 17.2L8 13.2L12 17.2L13 16.2L9 12.2L13 8.2ZM19 1H9C7.9 1 7 1.9 7 3V6H9V4H19V20H9V18H7V21C7 22.1 7.9 23 9 23H19C20.1 23 21 22.1 21 21V3C21 1.9 20.1 1 19 1Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_2181">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



PhonelinkErase.propTypes = {
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

import React from "react";
import PropTypes from "prop-types";

export const PhonelinkRing = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_2187)">
        <path
          d="M20.1 7.7L19.1 8.7C20.9 10.5 20.9 13.3 19.1 15.2L20.1 16.2C22.6 13.9 22.6 10.1 20.1 7.7V7.7ZM18 9.8L17 10.8C17.5 11.5 17.5 12.4 17 13.1L18 14.1C19.2 12.9 19.2 11.1 18 9.8ZM14 1H4C2.9 1 2 1.9 2 3V21C2 22.1 2.9 23 4 23H14C15.1 23 16 22.1 16 21V3C16 1.9 15.1 1 14 1ZM14 20H4V4H14V20Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_2187">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



PhonelinkRing.propTypes = {
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

import React from "react";
import PropTypes from "prop-types";

export const SettingsRemote = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_948)">
        <path
          d="M14.9997 9H8.99973C8.44973 9 7.99973 9.45 7.99973 10V22C7.99973 22.55 8.44973 23 8.99973 23H14.9997C15.5497 23 15.9997 22.55 15.9997 22V10C15.9997 9.45 15.5497 9 14.9997 9ZM11.9997 15C10.8997 15 9.99973 14.1 9.99973 13C9.99973 11.9 10.8997 11 11.9997 11C13.0997 11 13.9997 11.9 13.9997 13C13.9997 14.1 13.0997 15 11.9997 15ZM7.04973 6.05L8.45973 7.46C9.36973 6.56 10.6197 6 11.9997 6C13.3797 6 14.6297 6.56 15.5397 7.46L16.9497 6.05C15.6797 4.78 13.9297 4 11.9997 4C10.0697 4 8.31973 4.78 7.04973 6.05ZM11.9997 0C8.95973 0 6.20973 1.23 4.21973 3.22L5.62973 4.63C7.25973 3.01 9.50973 2 11.9997 2C14.4897 2 16.7397 3.01 18.3597 4.64L19.7697 3.23C17.7897 1.23 15.0397 0 11.9997 0Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_948">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



SettingsRemote.propTypes = {
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

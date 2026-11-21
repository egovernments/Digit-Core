import React from "react";
import PropTypes from "prop-types";

export const SwitchAccount = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_176_1075)">
        <path
          d="M4 6H2V20C2 21.1 2.9 22 4 22H18V20H4V6ZM20 2H8C6.9 2 6 2.9 6 4V16C6 17.1 6.9 18 8 18H20C21.1 18 22 17.1 22 16V4C22 2.9 21.1 2 20 2ZM14 4C15.66 4 17 5.34 17 7C17 8.66 15.66 10 14 10C12.34 10 11 8.66 11 7C11 5.34 12.34 4 14 4ZM20 16H8V14.5C8 12.51 12 11.5 14 11.5C16 11.5 20 12.51 20 14.5V16Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_176_1075">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

SwitchAccount.propTypes = {
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

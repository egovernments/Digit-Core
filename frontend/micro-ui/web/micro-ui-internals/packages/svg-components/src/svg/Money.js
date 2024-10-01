import React from "react";
import PropTypes from "prop-types";

export const Money = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11240)">
        <path
          d="M5 8H7V16H5V8ZM12 8H9C8.45 8 8 8.45 8 9V15C8 15.55 8.45 16 9 16H12C12.55 16 13 15.55 13 15V9C13 8.45 12.55 8 12 8ZM11 14H10V10H11V14ZM18 8H15C14.45 8 14 8.45 14 9V15C14 15.55 14.45 16 15 16H18C18.55 16 19 15.55 19 15V9C19 8.45 18.55 8 18 8ZM17 14H16V10H17V14Z"
          fill={fill}
        />
        <path d="M2 4V20H22V4H2ZM4 18V6H20V18H4Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_1974_11240">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



Money.propTypes = {
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

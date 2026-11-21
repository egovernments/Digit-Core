import React from "react";
import PropTypes from "prop-types";

export const Luggage = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_176_741)">
        <path
          d="M17 6H15V3C15 2.45 14.55 2 14 2H10C9.45 2 9 2.45 9 3V6H7C5.9 6 5 6.9 5 8V19C5 20.1 5.9 21 7 21C7 21.55 7.45 22 8 22C8.55 22 9 21.55 9 21H15C15 21.55 15.45 22 16 22C16.55 22 17 21.55 17 21C18.1 21 19 20.1 19 19V8C19 6.9 18.1 6 17 6ZM9.5 18H8V9H9.5V18ZM12.75 18H11.25V9H12.75V18ZM13.5 6H10.5V3.5H13.5V6ZM16 18H14.5V9H16V18Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_176_741">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



Luggage.propTypes = {
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

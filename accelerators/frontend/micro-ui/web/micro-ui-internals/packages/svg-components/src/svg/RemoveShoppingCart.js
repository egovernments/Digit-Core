import React from "react";
import PropTypes from "prop-types";

export const RemoveShoppingCart = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_834)">
        <path
          d="M22.73 22.73L2.77 2.77002L2 2.00002L1.27 1.27002L0 2.54002L4.39 6.93002L6.6 11.59L5.25 14.04C5.09 14.32 5 14.65 5 15C5 16.1 5.9 17 7 17H14.46L15.84 18.38C15.34 18.74 15.01 19.33 15.01 20C15.01 21.1 15.9 22 17 22C17.67 22 18.26 21.67 18.62 21.16L21.46 24L22.73 22.73ZM7.42 15C7.28 15 7.17 14.89 7.17 14.75L7.2 14.63L8.1 13H10.46L12.46 15H7.42ZM15.55 13C16.3 13 16.96 12.59 17.3 11.97L20.88 5.48002C20.96 5.34002 21 5.17002 21 5.00002C21 4.45002 20.55 4.00002 20 4.00002H6.54L15.55 13ZM7 18C5.9 18 5.01 18.9 5.01 20C5.01 21.1 5.9 22 7 22C8.1 22 9 21.1 9 20C9 18.9 8.1 18 7 18Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_834">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



RemoveShoppingCart.propTypes = {
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

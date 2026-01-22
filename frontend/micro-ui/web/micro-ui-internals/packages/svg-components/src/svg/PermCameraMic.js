import React from "react";
import PropTypes from "prop-types";

export const PermCameraMic = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_731)">
        <path
          d="M20 5H16.83L15 3H9L7.17 5H4C2.9 5 2 5.9 2 7V19C2 20.1 2.9 21 4 21H11V18.91C8.17 18.43 6 15.97 6 13H8C8 15.21 9.79 17 12 17C14.21 17 16 15.21 16 13H18C18 15.97 15.83 18.43 13 18.91V21H20C21.1 21 22 20.1 22 19V7C22 5.9 21.1 5 20 5ZM14 13C14 14.1 13.1 15 12 15C10.9 15 10 14.1 10 13V9C10 7.9 10.9 7 12 7C13.1 7 14 7.9 14 9V13Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_731">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



PermCameraMic.propTypes = {
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

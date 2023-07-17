import React from "react";
import PropTypes from "prop-types";

export const Hail = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11037)">
        <path
          d="M12 6C10.9 6 10 5.1 10 4C10 2.9 10.9 2 12 2C13.1 2 14 2.9 14 4C14 5.1 13.1 6 12 6ZM17 2H19V2.4C18.9 4.6 18.2 6.3 16.7 7.5C16.2 7.9 15.6 8.2 15 8.4V22H13V16H11V22H9V10.1C8.7 10.2 8.5 10.3 8.4 10.4C7.5 11.1 7.01 12 7 13.5V14H5V13.5C5 11.5 5.71 9.91 7.11 8.71C8.21 7.81 10 7 12 7C14 7 14.68 6.54 15.48 5.94C16.48 5.14 17 4 17 2.5V2ZM4 16H7V22H4V16Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_11037">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



Hail.propTypes = {
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

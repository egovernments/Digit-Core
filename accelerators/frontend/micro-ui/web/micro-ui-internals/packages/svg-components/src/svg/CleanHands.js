import React from "react";
import PropTypes from "prop-types";

export const CleanHands = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_176_597)">
        <path
          d="M16.99 5L17.62 6.37L18.99 7L17.62 7.63L16.99 9L16.36 7.63L14.99 7L16.36 6.37L16.99 5ZM11 6.13V4H13C13.57 4 14.1 4.17 14.55 4.45L15.98 3.02C15.15 2.39 14.13 2 13 2C11.52 2 7.5 2 7.5 2V4H9V6.14C7.23 6.51 5.81 7.8 5.26 9.5H9.24L15 11.65V11.03C15 8.61 13.28 6.59 11 6.13ZM1 22H5V11H1V22ZM20 17H13L10.91 16.27L11.24 15.33L13 16H15.82C16.47 16 17 15.47 17 14.82C17 14.33 16.69 13.89 16.23 13.71L8.97 11H7V20.02L14 22L22 19C21.99 17.9 21.11 17 20 17ZM20 14C21.1 14 22 13.1 22 12C22 10.9 20 8 20 8C20 8 18 10.9 18 12C18 13.1 18.9 14 20 14Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_176_597">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



CleanHands.propTypes = {
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

import React from "react";
import PropTypes from "prop-types";

export const Tram = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11401)">
        <path
          d="M19 16.94V8.5C19 5.71 16.39 5.1 12.99 5.01L13.75 3.5H17V2H7V3.5H11.75L10.99 5.02C7.86 5.11 5 5.73 5 8.5V16.94C5 18.39 6.19 19.6 7.59 19.91L6 21.5V22H8.23L10.23 20H14L16 22H18V21.5L16.5 20H16.42C18.11 20 19 18.63 19 16.94ZM12 18.5C11.17 18.5 10.5 17.83 10.5 17C10.5 16.17 11.17 15.5 12 15.5C12.83 15.5 13.5 16.17 13.5 17C13.5 17.83 12.83 18.5 12 18.5ZM17 14H7V9H17V14Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_11401">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

Tram.propTypes = {
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

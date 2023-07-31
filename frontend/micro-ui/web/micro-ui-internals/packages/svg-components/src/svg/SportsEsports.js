import React from "react";
import PropTypes from "prop-types";

export const SportsEsports = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_176_981)">
        <path
          d="M21.5798 16.09L20.4898 8.43C20.2098 6.46 18.5198 5 16.5298 5H7.46982C5.47982 5 3.78982 6.46 3.50982 8.43L2.41982 16.09C2.19982 17.63 3.38982 19 4.93982 19C5.61982 19 6.25982 18.73 6.73982 18.25L8.99982 16H14.9998L17.2498 18.25C17.7298 18.73 18.3798 19 19.0498 19C20.6098 19 21.7998 17.63 21.5798 16.09ZM10.9998 11H8.99982V13H7.99982V11H5.99982V10H7.99982V8H8.99982V10H10.9998V11ZM14.9998 10C14.4498 10 13.9998 9.55 13.9998 9C13.9998 8.45 14.4498 8 14.9998 8C15.5498 8 15.9998 8.45 15.9998 9C15.9998 9.55 15.5498 10 14.9998 10ZM16.9998 13C16.4498 13 15.9998 12.55 15.9998 12C15.9998 11.45 16.4498 11 16.9998 11C17.5498 11 17.9998 11.45 17.9998 12C17.9998 12.55 17.5498 13 16.9998 13Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_176_981">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



SportsEsports.propTypes = {
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

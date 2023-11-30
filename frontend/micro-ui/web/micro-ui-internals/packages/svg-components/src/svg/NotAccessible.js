import React from "react";
import PropTypes from "prop-types";

export const NotAccessible = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_656)">
        <path
          d="M13.9996 11.05L10.5796 7.63C10.8996 7.29 11.3196 7.06 11.8096 7.02C12.2896 6.98 12.6496 7.09 13.0096 7.28C13.1996 7.38 13.3996 7.5 13.6396 7.74L14.9296 9.17C15.9096 10.25 17.4596 11.02 18.9996 11V13C17.2496 12.99 15.2896 12.12 13.9996 11.05ZM11.9996 6C13.0996 6 13.9996 5.1 13.9996 4C13.9996 2.9 13.0996 2 11.9996 2C10.8996 2 9.99965 2.9 9.99965 4C9.99965 5.1 10.8996 6 11.9996 6ZM2.80965 2.81L1.38965 4.22L9.99965 12.83V15C9.99965 16.1 10.8996 17 11.9996 17H14.1696L19.7796 22.61L21.1896 21.2L2.80965 2.81ZM9.99965 20C8.33965 20 6.99965 18.66 6.99965 17C6.99965 15.69 7.83965 14.59 8.99965 14.17V12.1C6.71965 12.56 4.99965 14.58 4.99965 17C4.99965 19.76 7.23965 22 9.99965 22C12.4196 22 14.4396 20.28 14.8996 18H12.8296C12.4196 19.16 11.3096 20 9.99965 20Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_656">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



NotAccessible.propTypes = {
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

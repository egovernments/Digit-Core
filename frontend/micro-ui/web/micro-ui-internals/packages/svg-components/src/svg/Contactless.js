import React from "react";
import PropTypes from "prop-types";

export const Contactless = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_253)">
        <path
          d="M12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 12 2ZM8.46 14.45L7.1 13.83C7.38 13.22 7.51 12.59 7.5 11.97C7.49 11.34 7.36 10.73 7.1 10.17L8.46 9.54C8.81 10.29 8.99 11.1 9 11.94C9.01 12.8 8.83 13.64 8.46 14.45ZM11.53 16.01L10.23 15.27C10.75 14.35 11.01 13.29 11.01 12.12C11.01 10.93 10.74 9.79 10.21 8.72L11.55 8.05C12.19 9.33 12.51 10.7 12.51 12.12C12.51 13.55 12.18 14.86 11.53 16.01ZM14.67 17.33L13.32 16.67C14.1 15.07 14.5 13.49 14.5 11.98C14.5 10.47 14.1 8.91 13.32 7.34L14.66 6.67C15.56 8.45 16 10.23 16 11.98C16 13.72 15.56 15.52 14.67 17.33Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_253">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



Contactless.propTypes = {
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

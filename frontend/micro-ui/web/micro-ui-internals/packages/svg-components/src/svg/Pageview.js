import React from "react";
import PropTypes from "prop-types";

export const Pageview = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_709)">
        <path
          d="M11.5 9C10.12 9 9 10.12 9 11.5C9 12.88 10.12 14 11.5 14C12.88 14 14 12.88 14 11.5C14 10.12 12.88 9 11.5 9ZM20 4H4C2.9 4 2 4.9 2 6V18C2 19.1 2.9 20 4 20H20C21.1 20 22 19.1 22 18V6C22 4.9 21.1 4 20 4ZM16.79 18.21L13.88 15.3C13.19 15.74 12.37 16 11.49 16C9.01 16 7 13.99 7 11.5C7 9.01 9.01 7 11.5 7C13.99 7 16 9.01 16 11.5C16 12.38 15.74 13.19 15.3 13.89L18.21 16.79L16.79 18.21Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_709">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



Pageview.propTypes = {
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

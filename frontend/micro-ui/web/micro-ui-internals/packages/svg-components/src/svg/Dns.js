import React from "react";
import PropTypes from "prop-types";

export const Dns = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_302)">
        <path
          d="M20 13H4C3.45 13 3 13.45 3 14V20C3 20.55 3.45 21 4 21H20C20.55 21 21 20.55 21 20V14C21 13.45 20.55 13 20 13ZM7 19C5.9 19 5 18.1 5 17C5 15.9 5.9 15 7 15C8.1 15 9 15.9 9 17C9 18.1 8.1 19 7 19ZM20 3H4C3.45 3 3 3.45 3 4V10C3 10.55 3.45 11 4 11H20C20.55 11 21 10.55 21 10V4C21 3.45 20.55 3 20 3ZM7 9C5.9 9 5 8.1 5 7C5 5.9 5.9 5 7 5C8.1 5 9 5.9 9 7C9 8.1 8.1 9 7 9Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_302">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



Dns.propTypes = {
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

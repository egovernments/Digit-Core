import React from "react";
import PropTypes from "prop-types";

export const PhoneEnabled = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_2196)">
        <path
          d="M17.38 10.79L15.18 8.59C14.9 8.31 14.82 7.92 14.93 7.57C15.3 6.45 15.5 5.25 15.5 4C15.5 3.45 15.95 3 16.5 3H20C20.55 3 21 3.45 21 4C21 13.39 13.39 21 4 21C3.45 21 3 20.55 3 20V16.51C3 15.96 3.45 15.51 4 15.51C5.24 15.51 6.45 15.31 7.57 14.94C7.92 14.82 8.32 14.91 8.59 15.18L10.79 17.38C13.62 15.93 15.94 13.62 17.38 10.79Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_2196">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



PhoneEnabled.propTypes = {
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

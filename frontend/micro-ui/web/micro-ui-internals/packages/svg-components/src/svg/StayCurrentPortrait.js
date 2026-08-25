import React from "react";
import PropTypes from "prop-types";

export const StayCurrentPortrait = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_2263)">
        <path
          d="M16.9998 1.01L6.99977 1C5.89977 1 5.00977 1.9 5.00977 3V21C5.00977 22.1 5.89977 23 6.99977 23H16.9998C18.0998 23 18.9998 22.1 18.9998 21V3C18.9998 1.9 18.0998 1.01 16.9998 1.01ZM16.9998 19H6.99977V5H16.9998V19Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_2263">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



StayCurrentPortrait.propTypes = {
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

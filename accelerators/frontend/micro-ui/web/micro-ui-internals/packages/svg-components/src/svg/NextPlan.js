import React from "react";
import PropTypes from "prop-types";

export const NextPlan = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_645)">
        <path
          d="M12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 12 2ZM18 13.97H13L15.26 11.71C14.35 10.65 13.01 9.97 11.5 9.97C9.13 9.97 7.15 11.63 6.64 13.85L5.68 13.53C6.32 10.91 8.68 8.97 11.5 8.97C13.28 8.97 14.87 9.76 15.97 11L18 8.97V13.97Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_645">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



NextPlan.propTypes = {
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

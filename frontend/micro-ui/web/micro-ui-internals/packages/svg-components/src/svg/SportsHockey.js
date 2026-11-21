import React from "react";
import PropTypes from "prop-types";

export const SportsHockey = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_176_1013)">
        <path d="M2 17V20H4V16H3C2.45 16 2 16.45 2 17Z" fill={fill} />
        <path d="M9 16.0001H5V20.0001L9.69 19.9901C10.07 19.9901 10.41 19.7801 10.58 19.4401L11.45 17.5401L9.86 14.0601L9 16.0001Z" fill={fill} />
        <path d="M21.71 16.29C21.53 16.11 21.28 16 21 16H20V20H22V17C22 16.72 21.89 16.47 21.71 16.29Z" fill={fill} />
        <path
          d="M13.5996 12.84L17.6496 4H14.2996L12.5396 7.97L12.0496 9.07L11.9996 9.21L9.69961 4H6.34961L10.3996 12.84L11.9196 16.16L11.9996 16.34L13.4196 19.44C13.5896 19.78 13.9296 19.99 14.3096 19.99L18.9996 20V16H14.9996L13.5996 12.84Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_176_1013">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



SportsHockey.propTypes = {
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

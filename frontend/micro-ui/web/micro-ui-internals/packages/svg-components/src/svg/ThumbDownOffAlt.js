import React from "react";
import PropTypes from "prop-types";

export const ThumbDownOffAlt = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1083)">
        <path
          d="M10.89 18.28L11.46 15.39C11.58 14.8 11.42 14.19 11.04 13.73C10.66 13.27 10.1 13 9.5 13H4V11.92L6.57 6H14.66C14.84 6 15 6.16 15 6.34V14.18L10.89 18.28ZM10 22L16.41 15.59C16.79 15.21 17 14.7 17 14.17V6.34C17 5.05 15.95 4 14.66 4H6.56C5.85 4 5.2 4.37 4.84 4.97L2.17 11.12C2.06 11.37 2 11.64 2 11.92V13C2 14.1 2.9 15 4 15H9.5L8.58 19.65C8.53 19.87 8.56 20.11 8.66 20.31C8.89 20.76 9.18 21.17 9.54 21.53L10 22ZM20 15H22V4H20C19.45 4 19 4.45 19 5V14C19 14.55 19.45 15 20 15Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_1083">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

ThumbDownOffAlt.propTypes = {
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

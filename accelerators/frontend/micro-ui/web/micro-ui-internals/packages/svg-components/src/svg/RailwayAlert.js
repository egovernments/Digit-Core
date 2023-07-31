import React from "react";
import PropTypes from "prop-types";

export const RailwayAlert = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11336)">
        <path
          d="M23 8.00002C23.0001 6.61548 22.5896 5.262 21.8204 4.11076C21.0513 2.95953 19.958 2.06224 18.6789 1.53239C17.3997 1.00253 15.9922 0.863907 14.6342 1.13405C13.2763 1.40418 12.029 2.07095 11.05 3.05002C10.3673 3.01262 9.68368 2.99595 9 3.00002C4.58 3.00002 1 3.50002 1 7.00002V17.5C1 18.4283 1.36875 19.3185 2.02513 19.9749C2.6815 20.6313 3.57174 21 4.5 21L3 22.5V23H15V22.5L13.5 21C14.4283 21 15.3185 20.6313 15.9749 19.9749C16.6313 19.3185 17 18.4283 17 17.5V14.92C18.6646 14.6798 20.187 13.8482 21.2888 12.5775C22.3906 11.3068 22.998 9.68187 23 8.00002ZM3 12V7.00002H9.08C8.82133 8.75618 9.24345 10.5448 10.26 12H3ZM9 19C7.9 19 7 18.1 7 17C7 15.9 7.9 15 9 15C10.1 15 11 15.9 11 17C11 18.1 10.1 19 9 19ZM16.71 12.94L16.51 12.97L16 13L15.53 12.98L15.37 12.96L15.08 12.92L14.88 12.88L14.66 12.82C14.5816 12.8027 14.5047 12.7793 14.43 12.75L14.3 12.7C13.1701 12.2939 12.2245 11.4939 11.6367 10.4469C11.0489 9.39996 10.8584 8.17615 11.1 7.00002C11.14 6.81002 11.19 6.63002 11.25 6.46002L11.3 6.32002L11.45 5.94002L11.52 5.79002L11.72 5.43002L11.79 5.31002L12.09 4.89002L12.11 4.87002C12.35 4.57002 12.63 4.30002 12.93 4.06002L12.94 4.05002L13.4 3.73002L13.43 3.71002C14.2113 3.25614 15.0965 3.01159 16 3.00002C17.254 3.01506 18.4565 3.50077 19.3692 4.36084C20.2818 5.22092 20.838 6.39255 20.9273 7.64342C21.0167 8.8943 20.6327 10.1331 19.8516 11.1141C19.0704 12.0952 17.9491 12.7469 16.71 12.94V12.94ZM15 4.00002H17V9.00002H15V4.00002ZM15 10H17V12H15V10Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_11336">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



RailwayAlert.propTypes = {
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

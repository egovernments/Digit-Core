import React from "react";
import PropTypes from "prop-types";

export const SportsHandball = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_176_1005)">
        <path
          d="M14.2693 6.00026C13.7193 6.95026 14.0493 8.18026 14.9993 8.73026C15.9493 9.28026 17.1793 8.95026 17.7293 8.00026C18.2793 7.05026 17.9493 5.82026 16.9993 5.27026C16.0493 4.72026 14.8193 5.05026 14.2693 6.00026Z"
          fill={fill}
        />
        <path
          d="M15.8398 10.4103C15.8398 10.4103 14.2098 9.47033 13.2398 8.91033C10.8598 7.53033 10.0398 4.47033 11.4198 2.09033L9.68976 1.09033C8.09976 3.83033 8.59977 7.21033 10.6598 9.40033L5.50977 18.3203L7.23977 19.3203L8.73977 16.7203L10.4698 17.7203L7.46977 22.9203L9.19977 23.9203L15.4898 13.0303C16.6298 14.5803 16.8198 16.7203 15.7998 18.4903L17.5298 19.4903C19.1298 16.7403 18.8098 12.9103 15.8398 10.4103Z"
          fill={fill}
        />
        <path
          d="M12.7497 3.80014C13.4697 4.21014 14.3797 3.97014 14.7997 3.25014C15.2097 2.53014 14.9697 1.62014 14.2497 1.20014C13.5297 0.790143 12.6197 1.03014 12.1997 1.75014C11.7897 2.47014 12.0297 3.39014 12.7497 3.80014Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_176_1005">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



SportsHandball.propTypes = {
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

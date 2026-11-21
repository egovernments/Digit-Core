import React from "react";
import PropTypes from "prop-types";

export const SportsFootball = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_176_987)">
        <path
          d="M3.0201 15.6201C2.9401 18.0401 3.3401 19.9601 3.6901 20.3101C4.0401 20.6601 5.9701 21.0701 8.3801 20.9801L3.0201 15.6201Z"
          fill={fill}
        />
        <path
          d="M13.0803 3.27979C10.7503 3.69979 8.29027 4.61979 6.46027 6.45979C4.63027 8.29979 3.70027 10.7498 3.28027 13.0798L10.9103 20.7098C13.2503 20.2998 15.7003 19.3698 17.5303 17.5298C19.3603 15.6898 20.2903 13.2398 20.7103 10.9098L13.0803 3.27979ZM9.90027 15.4998L8.50027 14.0998L14.1003 8.49979L15.5003 9.89979L9.90027 15.4998Z"
          fill={fill}
        />
        <path
          d="M20.9801 8.37992C21.0601 5.95992 20.6601 4.03992 20.3101 3.68992C19.9601 3.33992 18.0301 2.92992 15.6201 3.01992L20.9801 8.37992Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_176_987">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



SportsFootball.propTypes = {
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

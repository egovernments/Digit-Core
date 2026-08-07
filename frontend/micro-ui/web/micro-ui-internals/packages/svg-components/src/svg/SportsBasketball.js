import React from "react";
import PropTypes from "prop-types";

export const SportsBasketball = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_176_951)">
        <path d="M17.0898 11.0001H21.9498C21.7898 9.3901 21.2398 7.8901 20.4098 6.6001C18.6798 7.4301 17.4198 9.0501 17.0898 11.0001Z" fill={fill} />
        <path d="M6.9098 11.0001C6.5798 9.0501 5.3198 7.4301 3.5898 6.6001C2.7598 7.8901 2.2098 9.3901 2.0498 11.0001H6.9098Z" fill={fill} />
        <path d="M15.07 11C15.39 8.41005 16.95 6.21005 19.13 5.00005C17.53 3.37005 15.39 2.29005 13 2.05005V11H15.07Z" fill={fill} />
        <path
          d="M8.93012 11H11.0001V2.05005C8.61012 2.29005 6.46012 3.37005 4.87012 5.00005C7.05012 6.21005 8.61012 8.41005 8.93012 11Z"
          fill={fill}
        />
        <path d="M15.07 13H13V21.95C15.39 21.71 17.54 20.63 19.13 19C16.95 17.79 15.39 15.59 15.07 13Z" fill={fill} />
        <path d="M3.5898 17.4C5.3098 16.57 6.5798 14.94 6.9098 13H2.0498C2.2098 14.61 2.7598 16.11 3.5898 17.4Z" fill={fill} />
        <path d="M17.0898 13C17.4198 14.95 18.6798 16.57 20.4098 17.4C21.2398 16.11 21.7898 14.61 21.9498 13H17.0898V13Z" fill={fill} />
        <path d="M8.93012 13C8.61012 15.59 7.05012 17.79 4.87012 19C6.47012 20.63 8.61012 21.71 11.0001 21.95V13H8.93012Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_176_951">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



SportsBasketball.propTypes = {
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

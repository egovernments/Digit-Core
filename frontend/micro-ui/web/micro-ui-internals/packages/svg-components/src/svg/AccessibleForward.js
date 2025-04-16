import React from "react";
import PropTypes from "prop-types";

export const AccessibleForward = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg
      width={width}
      height={height}
      className={className}
      onClick={onClick}
      style={style}
      viewBox="0 0 24 24"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
    >
      <g clip-path="url(#clip0_105_16)">
        <path
          d="M17 6.54004C18.1046 6.54004 19 5.64461 19 4.54004C19 3.43547 18.1046 2.54004 17 2.54004C15.8954 2.54004 15 3.43547 15 4.54004C15 5.64461 15.8954 6.54004 17 6.54004Z"
          fill={fill}
        />
        <path
          d="M14 17H12C12 18.65 10.65 20 9 20C7.35 20 6 18.65 6 17C6 15.35 7.35 14 9 14V12C6.24 12 4 14.24 4 17C4 19.76 6.24 22 9 22C11.76 22 14 19.76 14 17ZM17 13.5H15.14L16.81 9.83C17.42 8.5 16.44 7 14.96 7H9.76C8.95 7 8.22 7.47 7.89 8.2L7.22 10L9.14 10.53L9.79 9H12L10.17 13.1C9.57 14.43 10.56 16 12.02 16H17V21H19V15.5C19 14.4 18.1 13.5 17 13.5Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_16">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

AccessibleForward.propTypes = {
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

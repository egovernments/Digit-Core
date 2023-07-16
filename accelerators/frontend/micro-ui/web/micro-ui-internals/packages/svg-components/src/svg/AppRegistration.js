import React from "react";
import PropTypes from "prop-types";

export const AppRegistration = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1974)">
        <path d="M14 4H10V8H14V4Z" fill={fill} />
        <path d="M8 16H4V20H8V16Z" fill={fill} />
        <path d="M8 10H4V14H8V10Z" fill={fill} />
        <path d="M8 4H4V8H8V4Z" fill={fill} />
        <path d="M14 12.42V10H10V14H12.42L14 12.42Z" fill={fill} />
        <path
          d="M20.88 11.29L19.71 10.12C19.55 9.96 19.29 9.96 19.13 10.12L18.25 11L20 12.75L20.88 11.87C21.04 11.71 21.04 11.45 20.88 11.29Z"
          fill={fill}
        />
        <path d="M11 18.2501V20.0001H12.75L19.42 13.3301L17.67 11.5801L11 18.2501Z" fill={fill} />
        <path d="M20 4H16V8H20V4Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_105_1974">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

AppRegistration.propTypes = {
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

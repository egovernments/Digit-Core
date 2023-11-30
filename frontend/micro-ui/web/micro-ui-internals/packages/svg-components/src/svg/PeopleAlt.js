import React from "react";
import PropTypes from "prop-types";

export const PeopleAlt = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_176_802)">
        <path
          fill-rule="evenodd"
          clip-rule="evenodd"
          d="M16.6699 13.1299C18.0399 14.0599 18.9999 15.3199 18.9999 16.9999V19.9999H22.9999V16.9999C22.9999 14.8199 19.4299 13.5299 16.6699 13.1299Z"
          fill={fill}
        />
        <path d="M9 12C11.2091 12 13 10.2091 13 8C13 5.79086 11.2091 4 9 4C6.79086 4 5 5.79086 5 8C5 10.2091 6.79086 12 9 12Z" fill={fill} />
        <path
          fill-rule="evenodd"
          clip-rule="evenodd"
          d="M14.9999 12C17.2099 12 18.9999 10.21 18.9999 8C18.9999 5.79 17.2099 4 14.9999 4C14.5299 4 14.0899 4.1 13.6699 4.24C14.4999 5.27 14.9999 6.58 14.9999 8C14.9999 9.42 14.4999 10.73 13.6699 11.76C14.0899 11.9 14.5299 12 14.9999 12Z"
          fill={fill}
        />
        <path fill-rule="evenodd" clip-rule="evenodd" d="M9 13C6.33 13 1 14.34 1 17V20H17V17C17 14.34 11.67 13 9 13Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_176_802">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



PeopleAlt.propTypes = {
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

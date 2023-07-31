import React from "react";
import PropTypes from "prop-types";

export const Architecture = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_176_586)">
        <path
          d="M6.36035 18.7799L6.61035 20.9999L8.23035 19.4599L11.0004 11.8599C10.3204 11.6899 9.72035 11.3499 9.23035 10.8799L6.36035 18.7799Z"
          fill={fill}
        />
        <path d="M14.77 10.8799C14.28 11.3499 13.67 11.6899 13 11.8599L15.77 19.4599L17.39 20.9999L17.65 18.7799L14.77 10.8799Z" fill={fill} />
        <path
          d="M15 8C15 6.7 14.16 5.6 13 5.18V3H11V5.18C9.84 5.6 9 6.7 9 8C9 9.66 10.34 11 12 11C13.66 11 15 9.66 15 8ZM12 9C11.45 9 11 8.55 11 8C11 7.45 11.45 7 12 7C12.55 7 13 7.45 13 8C13 8.55 12.55 9 12 9Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_176_586">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

Architecture.propTypes = {
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

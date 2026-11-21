import React from "react";
import PropTypes from "prop-types";

export const OutgoingMail = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_699)">
        <path
          d="M18.5 11C18.67 11 18.84 11.01 19 11.03V6.87C19 5.84 18.16 5 17.13 5H3.87C2.84 5 2 5.84 2 6.87V17.13C2 18.16 2.84 19 3.87 19H13.6C13.22 18.25 13 17.4 13 16.5C13 13.46 15.46 11 18.5 11ZM10.4 13L4 9.19V7H4.23L10.41 10.68L16.74 7H17V9.16L10.4 13Z"
          fill={fill}
        />
        <path d="M19 13L17.59 14.41L19.17 16H15V18H19.17L17.59 19.59L19 21L23 17L19 13Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_105_699">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



OutgoingMail.propTypes = {
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

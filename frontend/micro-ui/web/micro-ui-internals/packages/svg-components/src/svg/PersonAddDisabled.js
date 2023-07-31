import React from "react";
import PropTypes from "prop-types";

export const PersonAddDisabled = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_2166)">
        <path d="M15 12C17.2091 12 19 10.2091 19 8C19 5.79086 17.2091 4 15 4C12.7909 4 11 5.79086 11 8C11 10.2091 12.7909 12 15 12Z" fill={fill} />
        <path
          d="M23 19.9998V17.9998C23 15.6998 18.9 14.2998 16.1 14.0998L22.1 19.9998H23ZM11.4 14.4998C9.2 15.0998 7 16.2998 7 17.9998V19.9998H16.9L20.9 23.9998L22.2 22.6998L1.2 1.7998L0 3.0998L4 7.0998V9.9998H1V11.9998H4V14.9998H6V11.9998H8.9L11.4 14.4998ZM6 9.9998V9.0998L6.9 9.9998H6Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_2166">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



PersonAddDisabled.propTypes = {
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

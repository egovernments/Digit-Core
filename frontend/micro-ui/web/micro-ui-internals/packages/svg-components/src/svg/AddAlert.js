import React from "react";
import PropTypes from "prop-types";

export const AddAlert = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1259)">
        <path
          d="M10.01 21.01C10.01 22.11 10.9 23 12 23C13.1 23 13.99 22.11 13.99 21.01H10.01ZM18.88 16.82V11C18.88 7.75 16.63 5.03 13.59 4.31V3.59C13.59 2.71 12.88 2 12 2C11.12 2 10.41 2.71 10.41 3.59V4.31C7.37 5.03 5.12 7.75 5.12 11V16.82L3 18.94V20H21V18.94L18.88 16.82ZM16 13.01H13V16.01H11V13.01H8V11H11V8H13V11H16V13.01Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_1259">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

AddAlert.propTypes = {
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

import React from "react";
import PropTypes from "prop-types";

export const TextRotationNone = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1071)">
        <path
          d="M12.75 3H11.25L6.5 14H8.6L9.5 11.8H14.5L15.4 14H17.5L12.75 3ZM10.13 10L12 4.98L13.87 10H10.13ZM20.5 18L17.5 15V17H5V19H17.5V21L20.5 18Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_1071">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

TextRotationNone.propTypes = {
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

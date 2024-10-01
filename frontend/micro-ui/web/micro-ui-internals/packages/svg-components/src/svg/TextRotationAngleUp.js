import React from "react";
import PropTypes from "prop-types";

export const TextRotationAngleUp = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1065)">
        <path
          d="M4.48969 4.20996L3.42969 5.26996L7.84969 16.4L9.32969 14.92L8.40969 12.73L11.9497 9.18996L14.1397 10.11L15.6197 8.62996L4.48969 4.20996ZM7.57969 11.01L5.35969 6.13996L10.2297 8.36996L7.57969 11.01ZM20.5697 9.32996H16.3297L17.7397 10.74L8.89969 19.58L10.3197 21L19.1597 12.16L20.5697 13.57V9.32996Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_1065">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

TextRotationAngleUp.propTypes = {
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

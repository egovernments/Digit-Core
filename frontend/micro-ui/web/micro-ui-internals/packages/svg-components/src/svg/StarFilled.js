import React from "react";
import PropTypes from "prop-types";

export const StarFilled = ({ className, id, onClick, style, percentage = 100, fill = "#F47738", width, height }) => {
  return (
    <svg
      xmlns="http://www.w3.org/2000/svg"
      enableBackground="new 0 0 24 24"
      className={className}
      style={style}
      onClick={onClick}
      viewBox="0 0 24 24"
      fill={fill}
      width={width}
      height={height}
    >
      <linearGradient id={id} x1="0" x2="1" y1="0" y2="0">
        <stop offset="0%" stopColor={fill} stopOpacity={1}></stop>
        <stop offset={`${percentage}%`} stopColor={fill} stopOpacity={1}></stop>
        <stop offset={`${percentage}%`} stopColor="white" stopOpacity={0}></stop>
      </linearGradient>
      <g>
        <path d="M0,0h24v24H0V0z" fill="none" />
        <path d="M0,0h24v24H0V0z" fill="none" />
      </g>
      <g>
        <path
          d="M12,17.27L18.18,21l-1.64-7.03L22,9.24l-7.19-0.61L12,2L9.19,8.63L2,9.24l5.46,4.73L5.82,21L12,17.27z"
          fill={`url(#${id})`}
          stroke={fill}
          strokeWidth={1}
        />
      </g>
    </svg>
  );
};

StarFilled.propTypes = {
  /**
   * Custom class name for the SVG icon.
   */
  className: PropTypes.string,

  /**
   * The ID of the linear gradient used for the star filling effect. Required.
   */
  id: PropTypes.string.isRequired,

  /**
   * Click event handler for the SVG icon.
   */
  onClick: PropTypes.func,

  /**
   * Custom style object for the SVG icon.
   */
  style: PropTypes.object,

  /**
   * The percentage of the star that is filled by the gradient (0 to 100).
   * Default is 100 (fully filled).
   */
  percentage: PropTypes.number,

  /**
   * Custom width of the SVG icon.
   */
  width: PropTypes.string,

  /**
   * Custom height of the SVG icon.
   */
  height: PropTypes.string,
};

import React from "react";
import PropTypes from "prop-types";

export const Build = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_171)">
        <path
          d="M22.7004 19L13.6004 9.90001C14.5004 7.60001 14.0004 4.90001 12.1004 3.00001C10.1004 1.00001 7.10043 0.600012 4.70043 1.70001L9.00043 6.00001L6.00043 9.00001L1.60043 4.70001C0.400428 7.10001 0.900428 10.1 2.90043 12.1C4.80043 14 7.50043 14.5 9.80043 13.6L18.9004 22.7C19.3004 23.1 19.9004 23.1 20.3004 22.7L22.6004 20.4C23.1004 20 23.1004 19.3 22.7004 19Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_171">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



Build.propTypes = {
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

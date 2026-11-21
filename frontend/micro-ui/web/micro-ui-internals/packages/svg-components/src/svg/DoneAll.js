import React from "react";
import PropTypes from "prop-types";

export const DoneAll = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_308)">
        <path
          d="M18.0002 7.00009L16.5902 5.59009L10.2502 11.9301L11.6602 13.3401L18.0002 7.00009ZM22.2402 5.59009L11.6602 16.1701L7.48016 12.0001L6.07016 13.4101L11.6602 19.0001L23.6602 7.00009L22.2402 5.59009ZM0.410156 13.4101L6.00016 19.0001L7.41016 17.5901L1.83016 12.0001L0.410156 13.4101Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_308">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



DoneAll.propTypes = {
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

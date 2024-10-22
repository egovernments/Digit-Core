import React from "react";
import PropTypes from "prop-types";

export const RingVolume = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_2236)">
        <path
          d="M23.71 16.67C20.66 13.78 16.54 12 12 12C7.46 12 3.34 13.78 0.29 16.67C0.11 16.85 0 17.1 0 17.38C0 17.66 0.11 17.91 0.29 18.09L2.77 20.57C2.95 20.75 3.2 20.86 3.48 20.86C3.75 20.86 4 20.75 4.18 20.58C4.97 19.84 5.87 19.22 6.84 18.73C7.17 18.57 7.4 18.23 7.4 17.83V14.73C8.85 14.25 10.4 14 12 14C13.6 14 15.15 14.25 16.6 14.72V17.82C16.6 18.21 16.83 18.56 17.16 18.72C18.14 19.21 19.03 19.84 19.82 20.57C20 20.75 20.25 20.85 20.52 20.85C20.8 20.85 21.05 20.74 21.23 20.56L23.71 18.08C23.89 17.9 24 17.65 24 17.37C24 17.1 23.89 16.85 23.71 16.67V16.67ZM21.16 6.26L19.75 4.85L16.19 8.4L17.6 9.81C17.6 9.81 21.05 6.29 21.16 6.26V6.26ZM13 2H11V7H13V2ZM6.4 9.81L7.81 8.4L4.26 4.84L2.84 6.26C2.95 6.29 6.4 9.81 6.4 9.81V9.81Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_2236">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



RingVolume.propTypes = {
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

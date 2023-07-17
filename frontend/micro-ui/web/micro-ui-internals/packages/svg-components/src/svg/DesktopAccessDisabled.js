import React from "react";
import PropTypes from "prop-types";

export const DesktopAccessDisabled = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_2051)">
        <path
          d="M23 15.9998C23 17.0998 22.1 17.9998 21 17.9998H20L18 15.9998H21V3.9998H6L4 1.9998H21C22.1 1.9998 23 2.8998 23 3.9998V15.9998ZM17.5 17.9998L15.5 15.9998L17.5 17.9998ZM14.9 17.9998L20.9 23.9998L22.2 22.6998L17.5 17.9998L15.5 15.9998L1.2 1.7998L0 3.0998L1 4.0998V15.9998C1 17.0998 1.9 17.9998 3 17.9998H10V19.9998H8V21.9998H16V19.9998H14V17.9998H14.9ZM3 15.9998V6.0998L12.9 15.9998H3Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_2051">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



DesktopAccessDisabled.propTypes = {
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

import React from "react";
import PropTypes from "prop-types";

export const DoneOutline = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_311)">
        <path
          d="M19.77 5.02995L21.17 6.42995L8.43 19.17L2.83 13.57L4.23 12.17L8.43 16.37L19.77 5.02995ZM19.77 2.19995L8.43 13.54L4.23 9.33995L0 13.57L8.43 22L24 6.42995L19.77 2.19995Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_311">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



DoneOutline.propTypes = {
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

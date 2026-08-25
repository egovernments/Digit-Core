import React from "react";
import PropTypes from "prop-types";

export const NoSim = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_2158)">
        <path
          d="M18.9899 5C18.9899 3.9 18.0999 3 16.9999 3H9.99988L7.65988 5.34L18.9999 16.68L18.9899 5ZM3.64988 3.88L2.37988 5.15L4.99988 7.77V19C4.99988 20.1 5.89988 21 6.99988 21H17.0099C17.3599 21 17.6799 20.9 17.9699 20.74L19.8499 22.62L21.1199 21.35L3.64988 3.88Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_2158">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



NoSim.propTypes = {
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

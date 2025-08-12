import React from "react";
import PropTypes from "prop-types";

export const Rtt = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_2243)">
        <path
          d="M9.03 3L7.92 10.07H10.54L11.24 5.57H13.82L11.8 18.43H9.47L9.06 21H16.33L16.73 18.43H14.38L16.38 5.57H18.96L18.25 10.07H20.9L22 3H9.03ZM8 5H4L3.69 7H7.69L8 5ZM7.39 9H3.39L3.08 11H7.08L7.39 9ZM8.31 17H2.31L2 19H8L8.31 17ZM8.93 13H2.93L2.62 15H8.63L8.93 13Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_2243">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



Rtt.propTypes = {
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

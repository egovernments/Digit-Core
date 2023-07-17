import React from "react";
import PropTypes from "prop-types";

export const SpeakerNotesOff = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_981)">
        <path
          d="M10.54 11L10 10.46L7.54 7.99998L6 6.45998L2.38 2.83998L1.27 1.72998L0 2.99998L2.01 5.00998L2 22L6 18H15L20.73 23.73L22 22.46L17.54 18L10.54 11ZM8 14H6V12H8V14ZM6 11V8.99998L8 11H6ZM20 1.99998H4.08L10 7.91998V5.99998H18V7.99998H10.08L11.08 8.99998H18V11H13.08L20.07 17.99C21.14 17.95 22 17.08 22 16V3.99998C22 2.89998 21.1 1.99998 20 1.99998Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_981">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



SpeakerNotesOff.propTypes = {
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

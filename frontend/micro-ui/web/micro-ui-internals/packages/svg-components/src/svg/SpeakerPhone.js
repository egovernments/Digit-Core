import React from "react";
import PropTypes from "prop-types";

export const SpeakerPhone = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_2257)">
        <path
          d="M7 7.07L8.43 8.5C9.34 7.59 10.61 7.02 12 7.02C13.39 7.02 14.66 7.59 15.57 8.5L17 7.07C15.72 5.79 13.95 5 12 5C10.05 5 8.28 5.79 7 7.07ZM12 1C8.98 1 6.24 2.23 4.25 4.21L5.66 5.62C7.28 4 9.53 3 12 3C14.47 3 16.72 4 18.34 5.62L19.75 4.21C17.76 2.23 15.02 1 12 1ZM14.86 10.01L9.14 10C8.51 10 8 10.51 8 11.14V20.85C8 21.48 8.51 21.99 9.14 21.99H14.85C15.48 21.99 15.99 21.48 15.99 20.85V11.14C16 10.51 15.49 10.01 14.86 10.01V10.01ZM15 20H9V12H15V20Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_2257">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



SpeakerPhone.propTypes = {
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

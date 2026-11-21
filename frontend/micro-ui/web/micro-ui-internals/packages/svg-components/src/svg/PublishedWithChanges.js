import React from "react";
import PropTypes from "prop-types";

export const PublishedWithChanges = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_802)">
        <path
          d="M17.66 9.5298L10.59 16.5998L6.35 12.3598L7.76 10.9498L10.59 13.7798L16.25 8.1198L17.66 9.5298ZM4 11.9998C4 9.6698 5.02 7.5798 6.62 6.1198L9 8.4998V2.4998H3L5.2 4.6998C3.24 6.5198 2 9.1098 2 11.9998C2 17.1898 5.95 21.4498 11 21.9498V19.9298C7.06 19.4398 4 16.0698 4 11.9998ZM22 11.9998C22 6.8098 18.05 2.5498 13 2.0498V4.0698C16.94 4.5598 20 7.9298 20 11.9998C20 14.3298 18.98 16.4198 17.38 17.8798L15 15.4998V21.4998H21L18.8 19.2998C20.76 17.4798 22 14.8898 22 11.9998Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_802">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



PublishedWithChanges.propTypes = {
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

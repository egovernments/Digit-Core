import React from "react";
import PropTypes from "prop-types";

export const PictureInPictureAlt = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_765)">
        <path
          d="M19 11H11V17H19V11ZM23 19V4.98C23 3.88 22.1 3 21 3H3C1.9 3 1 3.88 1 4.98V19C1 20.1 1.9 21 3 21H21C22.1 21 23 20.1 23 19ZM21 19.02H3V4.97H21V19.02Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_765">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



PictureInPictureAlt.propTypes = {
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

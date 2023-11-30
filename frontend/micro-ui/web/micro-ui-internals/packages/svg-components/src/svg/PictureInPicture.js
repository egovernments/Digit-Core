import React from "react";
import PropTypes from "prop-types";

export const PictureInPicture = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_762)">
        <path
          d="M19 7H11V13H19V7ZM21 3H3C1.9 3 1 3.9 1 5V19C1 20.1 1.9 20.98 3 20.98H21C22.1 20.98 23 20.1 23 19V5C23 3.9 22.1 3 21 3ZM21 19.01H3V4.98H21V19.01V19.01Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_762">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



PictureInPicture.propTypes = {
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

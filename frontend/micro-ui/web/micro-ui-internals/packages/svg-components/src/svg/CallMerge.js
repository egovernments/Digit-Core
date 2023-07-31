import React from "react";
import PropTypes from "prop-types";

export const CallMerge = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1999)">
        <path
          d="M16.9998 20.41L18.4098 19L14.9998 15.59L13.5898 17L16.9998 20.41ZM7.49984 8H10.9998V13.59L5.58984 19L6.99984 20.41L12.9998 14.41V8H16.4998L11.9998 3.5L7.49984 8Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_1999">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



CallMerge.propTypes = {
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

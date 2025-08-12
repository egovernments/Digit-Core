import React from "react";
import PropTypes from "prop-types";

export const LabelImportantOutline = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_553)">
        <path
          d="M15 19H3L7.5 12L3 5H15C15.65 5 16.26 5.31 16.63 5.84L21 12L16.63 18.16C16.26 18.68 15.65 19 15 19ZM6.5 17H15L18.5 12L15 7H6.5L10 12L6.5 17Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_553">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



LabelImportantOutline.propTypes = {
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

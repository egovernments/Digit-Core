import React from "react";
import PropTypes from "prop-types";

export const ReadMore = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_2227)">
        <path d="M22 7H13V9H22V7Z" fill={fill} />
        <path d="M22 15H13V17H22V15Z" fill={fill} />
        <path d="M22 11H16V13H22V11Z" fill={fill} />
        <path d="M13 12L8 7V11H2V13H8V17L13 12Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_105_2227">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



ReadMore.propTypes = {
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

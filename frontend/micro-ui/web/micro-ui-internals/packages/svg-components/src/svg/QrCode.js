import React from "react";
import PropTypes from "prop-types";

export const QrCode = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_2208)">
        <path d="M3 11H11V3H3V11ZM5 5H9V9H5V5Z" fill={fill} />
        <path d="M3 21H11V13H3V21ZM5 15H9V19H5V15Z" fill={fill} />
        <path d="M13 3V11H21V3H13ZM19 9H15V5H19V9Z" fill={fill} />
        <path d="M21 19H19V21H21V19Z" fill={fill} />
        <path d="M15 13H13V15H15V13Z" fill={fill} />
        <path d="M17 15H15V17H17V15Z" fill={fill} />
        <path d="M15 17H13V19H15V17Z" fill={fill} />
        <path d="M17 19H15V21H17V19Z" fill={fill} />
        <path d="M19 17H17V19H19V17Z" fill={fill} />
        <path d="M19 13H17V15H19V13Z" fill={fill} />
        <path d="M21 15H19V17H21V15Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_105_2208">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



QrCode.propTypes = {
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

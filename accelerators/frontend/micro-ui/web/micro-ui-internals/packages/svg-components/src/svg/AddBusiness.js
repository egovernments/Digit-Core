import React from "react";
import PropTypes from "prop-types";

export const AddBusiness = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_10787)">
        <path d="M15 17H17V14H18V12L17 7H2L1 12V14H2V20H11V14H15V17ZM9 18H4V14H9V18Z" fill={fill} />
        <path d="M17 4H2V6H17V4Z" fill={fill} />
        <path d="M20 18V15H18V18H15V20H18V23H20V20H23V18H20Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_1974_10787">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

AddBusiness.propTypes = {
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

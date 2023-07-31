import React from "react";
import PropTypes from "prop-types";

export const Gavel = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_439)">
        <path d="M8.08 5.24205L5.25 8.06885L19.384 22.2188L22.214 19.392L8.08 5.24205Z" fill={fill} />
        <path d="M12.3143 0.998348L9.48633 3.82715L15.1439 9.48315L17.9719 6.65435L12.3143 0.998348Z" fill={fill} />
        <path d="M3.82742 9.48654L0.999023 12.3149L6.65582 17.9717L9.48422 15.1433L3.82742 9.48654Z" fill={fill} />
        <path d="M13 21H1V23H13V21Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_105_439">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



Gavel.propTypes = {
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

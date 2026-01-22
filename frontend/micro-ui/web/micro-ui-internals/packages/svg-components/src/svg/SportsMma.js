import React from "react";
import PropTypes from "prop-types";

export const SportsMma = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_176_1032)">
        <path d="M7 20C7 20.55 7.45 21 8 21H16C16.55 21 17 20.55 17 20V17H7V20Z" fill={fill} />
        <path
          d="M18 7C17.45 7 17 7.45 17 8V5C17 3.9 16.1 3 15 3H7C5.9 3 5 3.9 5 5V10.8C5 10.93 5.01 11.06 5.04 11.19L5.84 15.19C5.93 15.66 6.34 15.99 6.82 15.99H17.18C17.63 15.99 18.07 15.63 18.16 15.19L18.96 11.19C18.99 11.06 19 10.93 19 10.8V8C19 7.45 18.55 7 18 7ZM15 10H7V7H15V10Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_176_1032">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



SportsMma.propTypes = {
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

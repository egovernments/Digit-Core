import React from "react";
import PropTypes from "prop-types";

export const EditAttributes = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_10961)">
        <path
          d="M17.63 7H6.37C3.96 7 2 9.24 2 12C2 14.76 3.96 17 6.37 17H17.63C20.04 17 22 14.76 22 12C22 9.24 20.04 7 17.63 7ZM7.24 14.46L4.67 11.89L5.37 11.19L7.24 13.06L10.76 9.54L11.46 10.24L7.24 14.46V14.46Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_10961">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



EditAttributes.propTypes = {
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

import React from "react";
import PropTypes from "prop-types";

export const Nightlife = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11274)">
        <path
          d="M1 5H15L9 14V18H11V20H5V18H7V14L1 5ZM10.1 9L11.5 7H4.49L5.89 9H10.1ZM17 5H22V8H19V17C19 18.66 17.66 20 16 20C14.34 20 13 18.66 13 17C13 15.34 14.34 14 16 14C16.35 14 16.69 14.06 17 14.17V5Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_11274">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



Nightlife.propTypes = {
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

import React from "react";
import PropTypes from "prop-types";

export const ShopAlt = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_967)">
        <path
          d="M3 9H1V20C1 21.11 1.89 22 3 22H17C18.11 22 19 21.11 19 20H3V9ZM18 5V3C18 1.89 17.11 1 16 1H12C10.89 1 10 1.89 10 3V5H5V16C5 17.11 5.89 18 7 18H21C22.11 18 23 17.11 23 16V5H18ZM12 3H16V5H12V3ZM12 15V8L17.5 11L12 15Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_967">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



ShopAlt.propTypes = {
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

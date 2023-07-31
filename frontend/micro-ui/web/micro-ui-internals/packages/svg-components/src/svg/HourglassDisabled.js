import React from "react";
import PropTypes from "prop-types";

export const HourglassDisabled = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_505)">
        <path d="M8 4H16V7.5L13.16 10.34L14.41 11.59L18 8.01L17.99 8H18V2H6V3.17L8 5.17V4Z" fill={fill} />
        <path
          d="M2.10043 2.1001L0.69043 3.5101L9.59043 12.4101L6.00043 16.0001L6.01043 16.0101H6.00043V22.0001H18.0004V20.8301L20.4904 23.3201L21.9004 21.9101L2.10043 2.1001ZM16.0004 20.0001H8.00043V16.5001L10.8404 13.6601L16.0004 18.8301V20.0001Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_505">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



HourglassDisabled.propTypes = {
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

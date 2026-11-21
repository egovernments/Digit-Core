import React from "react";
import PropTypes from "prop-types";

export const SportsRugby = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_176_1046)">
        <path
          d="M20.4902 3.51004C19.9302 2.95004 18.3402 2.54004 16.3302 2.54004C13.2502 2.54004 9.18025 3.50004 6.35025 6.33004C1.66025 11.03 2.10025 19.07 3.51025 20.49C4.07025 21.05 5.66025 21.46 7.67025 21.46C10.7502 21.46 14.8202 20.5 17.6502 17.67C22.3402 12.97 21.9002 4.93004 20.4902 3.51004ZM7.76025 7.76004C10.4003 5.12004 14.1102 4.64004 15.7902 4.57004C13.7402 5.51004 11.3303 7.02004 9.18025 9.18004C7.02025 11.34 5.51025 13.76 4.56025 15.81C4.66025 13.33 5.44025 10.07 7.76025 7.76004ZM16.2402 16.24C13.6002 18.88 9.89025 19.36 8.21025 19.43C10.2602 18.49 12.6702 16.98 14.8202 14.82C16.9802 12.66 18.4903 10.24 19.4403 8.19004C19.3403 10.67 18.5602 13.93 16.2402 16.24Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_176_1046">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



SportsRugby.propTypes = {
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

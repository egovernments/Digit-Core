import React from "react";
import PropTypes from "prop-types";

export const BakeryDining = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_10838)">
        <path
          fill-rule="evenodd"
          clip-rule="evenodd"
          d="M19.2802 16.34C18.0702 15.45 17.4602 15 17.4602 15C17.4602 15 17.7802 14.41 18.4202 13.22C18.8002 12.63 19.6402 12.63 20.0202 13.22L20.8302 14.48C21.0202 14.78 21.0402 15.16 20.8902 15.48L20.6702 15.95C20.4202 16.49 19.7602 16.67 19.2802 16.34ZM4.72024 16.34C4.24024 16.67 3.59024 16.49 3.33024 15.96L3.10024 15.49C2.95024 15.17 2.97024 14.79 3.16024 14.49L3.97024 13.23C4.35024 12.64 5.19024 12.64 5.57024 13.23C6.22024 14.41 6.54024 15 6.54024 15C6.54024 15 5.93024 15.45 4.72024 16.34ZM15.3602 9.37C15.4502 8.69 16.0902 8.31 16.6302 8.62L18.2202 9.52C18.6802 9.78 18.8502 10.43 18.5802 10.93L16.5002 15H14.7002L15.3602 9.37ZM8.63024 9.37L9.30024 15H7.50024L5.41024 10.92C5.14024 10.42 5.31024 9.77 5.77024 9.51L7.36024 8.61C7.89024 8.31 8.54024 8.69 8.63024 9.37ZM13.8002 15H10.2002L9.46024 8.12C9.39024 7.53 9.81024 7 10.3402 7H13.6402C14.1702 7 14.5802 7.53 14.5202 8.12L13.8002 15Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_10838">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



BakeryDining.propTypes = {
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

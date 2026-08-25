import React from "react";
import PropTypes from "prop-types";

export const PrintDisabled = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_2205)">
        <path
          d="M19.1 16.9998H22V10.9998C22 9.2998 20.7 7.9998 19 7.9998H10L19.1 16.9998ZM19 9.9998C19.55 9.9998 20 10.4498 20 10.9998C20 11.5498 19.55 11.9998 19 11.9998C18.45 11.9998 18 11.5498 18 10.9998C18 10.4498 18.45 9.9998 19 9.9998ZM18 6.9998V2.9998H6V4.0998L9 6.9998H18ZM1.2 1.7998L0 2.9998L4.9 7.9998C3.3 8.0998 2 9.3998 2 10.9998V16.9998H6V20.9998H17.9L20.9 23.9998L22.2 22.6998L1.2 1.7998V1.7998ZM8 18.9998V13.9998H10.9L15.9 18.9998H8Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_2205">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



PrintDisabled.propTypes = {
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

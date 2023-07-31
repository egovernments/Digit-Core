import React from "react";
import PropTypes from "prop-types";

export const Update = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1143)">
        <path
          d="M20.9998 10.12H14.2198L16.9598 7.3C14.2298 4.6 9.80976 4.5 7.07976 7.2C4.34976 9.91 4.34976 14.28 7.07976 16.99C9.80976 19.7 14.2298 19.7 16.9598 16.99C18.3198 15.65 18.9998 14.08 18.9998 12.1H20.9998C20.9998 14.08 20.1198 16.65 18.3598 18.39C14.8498 21.87 9.14976 21.87 5.63976 18.39C2.13976 14.92 2.10976 9.28 5.61976 5.81C9.12976 2.34 14.7598 2.34 18.2698 5.81L20.9998 3V10.12ZM12.4998 8V12.25L15.9998 14.33L15.2798 15.54L10.9998 13V8H12.4998Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_1143">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

Update.propTypes = {
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

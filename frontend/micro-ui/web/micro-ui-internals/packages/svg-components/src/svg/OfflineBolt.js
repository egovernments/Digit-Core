import React from "react";
import PropTypes from "prop-types";

export const OfflineBolt = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_664)">
        <path
          d="M11.9995 2.02002C6.48953 2.02002 2.01953 6.49002 2.01953 12C2.01953 17.51 6.48953 21.98 11.9995 21.98C17.5095 21.98 21.9795 17.51 21.9795 12C21.9795 6.49002 17.5095 2.02002 11.9995 2.02002ZM11.4795 20V13.74H7.99953L12.9995 4.00002V10.26H16.3495L11.4795 20Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_664">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



OfflineBolt.propTypes = {
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

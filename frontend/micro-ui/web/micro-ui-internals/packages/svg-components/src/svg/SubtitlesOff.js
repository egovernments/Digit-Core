import React from "react";
import PropTypes from "prop-types";

export const SubtitlesOff = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1003)">
        <path
          d="M20.0001 4H6.83008L14.8301 12H20.0001V14H16.8301L21.7601 18.93C21.9101 18.65 22.0001 18.34 22.0001 18V6C22.0001 4.9 21.1001 4 20.0001 4Z"
          fill={fill}
        />
        <path
          d="M1.04004 3.8702L2.24004 5.0702C2.09004 5.3502 2.00004 5.6602 2.00004 6.0002V18.0002C2.00004 19.1002 2.90004 20.0002 4.00004 20.0002H17.17L20.13 22.9602L21.54 21.5502L2.45004 2.4502L1.04004 3.8702ZM8.00004 12.0002V14.0002H4.00004V12.0002H8.00004ZM14 16.8302V18.0002H4.00004V16.0002H13.17L14 16.8302Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_1003">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



SubtitlesOff.propTypes = {
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

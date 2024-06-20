import React from "react";
import PropTypes from "prop-types";

export const HistoryEdu = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_176_721)">
        <path
          d="M9.00043 4V5.38C8.17043 5.05 7.28043 4.88 6.39043 4.88C4.60043 4.88 2.81043 5.56 1.44043 6.93L4.77043 10.26H5.88043V11.37C6.74043 12.23 7.86043 12.68 8.99043 12.73V15H6.00043V18C6.00043 19.1 6.90043 20 8.00043 20H18.0004C19.6604 20 21.0004 18.66 21.0004 17V4H9.00043ZM7.89043 10.41V8.26H5.61043L4.57043 7.22C5.14043 7 5.76043 6.88 6.39043 6.88C7.73043 6.88 8.98043 7.4 9.93043 8.34L11.3404 9.75L11.1404 9.95C10.6304 10.46 9.95043 10.75 9.22043 10.75C8.75043 10.75 8.29043 10.63 7.89043 10.41ZM19.0004 17C19.0004 17.55 18.5504 18 18.0004 18C17.4504 18 17.0004 17.55 17.0004 17V15H11.0004V12.41C11.5704 12.18 12.1004 11.84 12.5604 11.38L12.7604 11.18L15.5904 14H17.0004V12.59L11.0004 6.62V6H19.0004V17Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_176_721">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



HistoryEdu.propTypes = {
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

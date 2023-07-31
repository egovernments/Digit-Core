import React from "react";
import PropTypes from "prop-types";

export const BuildCircle = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_174)">
        <path
          fill-rule="evenodd"
          clip-rule="evenodd"
          d="M12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 12 2ZM16.9 15.49L15.5 16.89C15.3 17.09 14.99 17.09 14.79 16.89L11.38 13.48C10.16 13.91 8.74 13.65 7.76 12.67C6.65 11.56 6.46 9.88 7.17 8.57L9.52 10.92L10.93 9.51L8.58 7.17C9.9 6.46 11.57 6.65 12.68 7.76C13.66 8.74 13.92 10.16 13.49 11.38L16.9 14.79C17.09 14.98 17.09 15.3 16.9 15.49Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_174">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



BuildCircle.propTypes = {
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

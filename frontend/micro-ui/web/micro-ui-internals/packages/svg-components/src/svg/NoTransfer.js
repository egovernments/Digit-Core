import React from "react";
import PropTypes from "prop-types";

export const NoTransfer = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11288)">
        <path
          d="M21.1896 21.19L2.80965 2.81L1.38965 4.22L3.99965 6.83V16C3.99965 16.88 4.38965 17.67 4.99965 18.22V20C4.99965 20.55 5.44965 21 5.99965 21H6.99965C7.54965 21 7.99965 20.55 7.99965 20V19H15.9996V20C15.9996 20.55 16.4496 21 16.9996 21H17.9996C18.0496 21 18.0896 20.98 18.1396 20.97L19.7796 22.61L21.1896 21.19ZM7.49965 17C6.66965 17 5.99965 16.33 5.99965 15.5C5.99965 14.67 6.66965 14 7.49965 14C8.32965 14 8.99965 14.67 8.99965 15.5C8.99965 16.33 8.32965 17 7.49965 17ZM5.99965 11V8.83L8.16965 11H5.99965ZM8.82965 6L5.77965 2.95C7.23965 2.16 9.47965 2 11.9996 2C16.4196 2 19.9996 2.5 19.9996 6V16C19.9996 16.35 19.9196 16.67 19.8096 16.98L13.8296 11H17.9996V6H8.82965Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_11288">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



NoTransfer.propTypes = {
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

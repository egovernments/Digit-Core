import React from "react";
import PropTypes from "prop-types";

export const PublicOff = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_176_863)">
        <path
          d="M10.9996 8.17L6.48965 3.66C8.06965 2.61 9.95965 2 11.9996 2C17.5196 2 21.9996 6.48 21.9996 12C21.9996 14.04 21.3896 15.93 20.3396 17.51L18.8796 16.05C19.5896 14.87 19.9996 13.48 19.9996 12C19.9996 8.65 17.9296 5.78 14.9996 4.59V5C14.9996 6.1 14.0996 7 12.9996 7H10.9996V8.17ZM21.1896 21.19L19.7796 22.6L17.5096 20.33C15.9296 21.39 14.0396 22 11.9996 22C6.47965 22 1.99965 17.52 1.99965 12C1.99965 9.96 2.60965 8.07 3.65965 6.49L1.38965 4.22L2.79965 2.81L21.1896 21.19ZM10.9996 18C9.89965 18 8.99965 17.1 8.99965 16V15L4.20965 10.21C4.07965 10.79 3.99965 11.38 3.99965 12C3.99965 16.08 7.04965 19.44 10.9996 19.93V18Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_176_863">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



PublicOff.propTypes = {
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

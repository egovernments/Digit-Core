import React from "react";
import PropTypes from "prop-types";

export const WifiProtectedSetup = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1221)">
        <path
          d="M16.71 5.29L19 3H11V11L13.3 8.7C15.27 10.16 16.55 12.48 16.55 15.12C16.55 16.43 16.23 17.66 15.67 18.75C18 17.23 19.55 14.61 19.55 11.62C19.55 9.1 18.44 6.85 16.71 5.29Z"
          fill={fill}
        />
        <path
          d="M7.45996 8.88C7.45996 7.57 7.77996 6.34 8.33996 5.25C5.99996 6.77 4.45996 9.39 4.45996 12.38C4.45996 14.9 5.55996 17.15 7.29996 18.71L4.99996 21H13V13L10.7 15.3C8.73996 13.84 7.45996 11.52 7.45996 8.88Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_1221">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

WifiProtectedSetup.propTypes = {
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

import React from "react";
import PropTypes from "prop-types";

export const SportsMotorsports = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_176_1039)">
        <path
          d="M11.9996 11.3898C11.9996 10.7398 11.6096 10.1598 11.0196 9.9098L5.43965 7.5498C3.95965 9.2298 3.11965 11.2498 2.63965 12.9998H10.3896C11.2796 12.9998 11.9996 12.2798 11.9996 11.3898Z"
          fill={fill}
        />
        <path
          d="M21.96 11.2199C21.55 6.80986 17.4 3.72986 12.98 4.01986C10.47 4.17986 8.54 4.95986 7.05 6.05986L11.79 8.06986C13.12 8.63986 13.99 9.93986 13.99 11.3899C13.99 13.3799 12.37 14.9999 10.38 14.9999H2.21C2 16.3099 2 17.1999 2 17.1999V17.9999C2 19.0999 2.9 19.9999 4 19.9999H14C18.67 19.9999 22.41 15.9899 21.96 11.2199Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_176_1039">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



SportsMotorsports.propTypes = {
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

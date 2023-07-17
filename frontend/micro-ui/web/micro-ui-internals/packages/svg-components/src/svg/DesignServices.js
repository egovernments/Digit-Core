import React from "react";
import PropTypes from "prop-types";

export const DesignServices = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_10914)">
        <path
          d="M16.24 11.5099L17.81 9.93992L14.06 6.18992L12.49 7.75992L8.35 3.62992C7.57 2.84992 6.3 2.84992 5.52 3.62992L3.62 5.52992C2.84 6.30992 2.84 7.57992 3.62 8.35992L7.75 12.4899L3 17.2499V20.9999H6.75L11.51 16.2399L15.64 20.3699C16.59 21.3199 17.87 20.9699 18.47 20.3699L20.37 18.4699C21.15 17.6899 21.15 16.4199 20.37 15.6399L16.24 11.5099ZM9.18 11.0699L5.04 6.93992L6.93 5.03992L8.2 6.30992L7.02 7.49992L8.43 8.90992L9.62 7.71992L11.07 9.16992L9.18 11.0699ZM17.06 18.9599L12.93 14.8299L14.83 12.9299L16.28 14.3799L15.09 15.5699L16.5 16.9799L17.69 15.7899L18.96 17.0599L17.06 18.9599Z"
          fill={fill}
        />
        <path
          d="M20.7099 7.03987C21.0999 6.64987 21.0999 6.01987 20.7099 5.62987L18.3699 3.28987C17.8999 2.81987 17.2499 2.99987 16.9599 3.28987L15.1299 5.11987L18.8799 8.86987L20.7099 7.03987Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_10914">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



DesignServices.propTypes = {
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

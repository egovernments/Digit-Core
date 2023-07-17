import React from "react";
import PropTypes from "prop-types";

export const NotificationsActive = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_176_769)">
        <path
          d="M7.58027 4.08L6.15027 2.65C3.75027 4.48 2.17027 7.3 2.03027 10.5H4.03027C4.18027 7.85 5.54027 5.53 7.58027 4.08V4.08ZM19.9703 10.5H21.9703C21.8203 7.3 20.2403 4.48 17.8503 2.65L16.4303 4.08C18.4503 5.53 19.8203 7.85 19.9703 10.5ZM18.0003 11C18.0003 7.93 16.3603 5.36 13.5003 4.68V4C13.5003 3.17 12.8303 2.5 12.0003 2.5C11.1703 2.5 10.5003 3.17 10.5003 4V4.68C7.63027 5.36 6.00027 7.92 6.00027 11V16L4.00027 18V19H20.0003V18L18.0003 16V11ZM12.0003 22C12.1403 22 12.2703 21.99 12.4003 21.96C13.0503 21.82 13.5803 21.38 13.8403 20.78C13.9403 20.54 13.9903 20.28 13.9903 20H9.99027C10.0003 21.1 10.8903 22 12.0003 22Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_176_769">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



NotificationsActive.propTypes = {
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

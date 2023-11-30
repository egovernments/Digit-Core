import React from "react";
import PropTypes from "prop-types";

export const NotificationsOff = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_176_775)">
        <path
          d="M20 18.69L7.84 6.14L5.27 3.49L4 4.76L6.8 7.56V7.57C6.28 8.56 6 9.73 6 10.99V15.99L4 17.99V18.99H17.73L19.73 20.99L21 19.72L20 18.69V18.69ZM12 22C13.11 22 14 21.11 14 20H10C10 21.11 10.89 22 12 22ZM18 14.68V11C18 7.92 16.36 5.36 13.5 4.68V4C13.5 3.17 12.83 2.5 12 2.5C11.17 2.5 10.5 3.17 10.5 4V4.68C10.35 4.71 10.21 4.76 10.08 4.8C9.98 4.83 9.88 4.87 9.78 4.91H9.77C9.76 4.91 9.76 4.91 9.75 4.92C9.52 5.01 9.29 5.12 9.07 5.23C9.07 5.23 9.06 5.23 9.06 5.24L18 14.68Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_176_775">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



NotificationsOff.propTypes = {
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

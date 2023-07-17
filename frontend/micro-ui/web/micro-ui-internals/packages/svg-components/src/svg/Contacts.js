import React from "react";
import PropTypes from "prop-types";

export const Contacts = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_2041)">
        <path
          d="M20 0H4V2H20V0ZM4 24H20V22H4V24ZM20 4H4C2.9 4 2 4.9 2 6V18C2 19.1 2.9 20 4 20H20C21.1 20 22 19.1 22 18V6C22 4.9 21.1 4 20 4ZM12 6.75C13.24 6.75 14.25 7.76 14.25 9C14.25 10.24 13.24 11.25 12 11.25C10.76 11.25 9.75 10.24 9.75 9C9.75 7.76 10.76 6.75 12 6.75ZM17 17H7V15.5C7 13.83 10.33 13 12 13C13.67 13 17 13.83 17 15.5V17Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_2041">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



Contacts.propTypes = {
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

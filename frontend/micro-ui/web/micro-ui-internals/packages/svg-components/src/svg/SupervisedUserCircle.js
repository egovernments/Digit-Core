import React from "react";
import PropTypes from "prop-types";

export const SupervisedUserCircle = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1010)">
        <path
          d="M11.9902 2C6.47023 2 1.99023 6.48 1.99023 12C1.99023 17.52 6.47023 22 11.9902 22C17.5102 22 21.9902 17.52 21.9902 12C21.9902 6.48 17.5102 2 11.9902 2V2ZM15.6002 8.34C16.6702 8.34 17.5302 9.2 17.5302 10.27C17.5302 11.34 16.6702 12.2 15.6002 12.2C14.5302 12.2 13.6702 11.34 13.6702 10.27C13.6602 9.2 14.5302 8.34 15.6002 8.34V8.34ZM9.60023 6.76C10.9002 6.76 11.9602 7.82 11.9602 9.12C11.9602 10.42 10.9002 11.48 9.60023 11.48C8.30024 11.48 7.24023 10.42 7.24023 9.12C7.24023 7.81 8.29024 6.76 9.60023 6.76ZM9.60023 15.89V19.64C7.20023 18.89 5.30023 17.04 4.46023 14.68C5.51023 13.56 8.13023 12.99 9.60023 12.99C10.1302 12.99 10.8002 13.07 11.5002 13.21C9.86023 14.08 9.60023 15.23 9.60023 15.89ZM11.9902 20C11.7202 20 11.4602 19.99 11.2002 19.96V15.89C11.2002 14.47 14.1402 13.76 15.6002 13.76C16.6702 13.76 18.5202 14.15 19.4402 14.91C18.2702 17.88 15.3802 20 11.9902 20V20Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_1010">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



SupervisedUserCircle.propTypes = {
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

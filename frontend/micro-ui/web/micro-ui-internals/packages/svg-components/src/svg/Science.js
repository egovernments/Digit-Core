import React from "react";
import PropTypes from "prop-types";

export const Science = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_176_882)">
        <path
          d="M19.8002 18.4L14.0002 10.67V6.5L15.3502 4.81C15.6102 4.48 15.3802 4 14.9602 4H9.04018C8.62018 4 8.39018 4.48 8.65018 4.81L10.0002 6.5V10.67L4.20018 18.4C3.71018 19.06 4.18018 20 5.00018 20H19.0002C19.8202 20 20.2902 19.06 19.8002 18.4Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_176_882">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



Science.propTypes = {
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

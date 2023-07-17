import React from "react";
import PropTypes from "prop-types";

export const ContactMail = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_2044)">
        <path
          d="M21 8V7L18 9L15 7V8L18 10L21 8ZM22 3H2C0.9 3 0 3.9 0 5V19C0 20.1 0.9 21 2 21H22C23.1 21 23.99 20.1 23.99 19L24 5C24 3.9 23.1 3 22 3ZM8 6C9.66 6 11 7.34 11 9C11 10.66 9.66 12 8 12C6.34 12 5 10.66 5 9C5 7.34 6.34 6 8 6ZM14 18H2V17C2 15 6 13.9 8 13.9C10 13.9 14 15 14 17V18ZM22 12H14V6H22V12Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_2044">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



ContactMail.propTypes = {
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

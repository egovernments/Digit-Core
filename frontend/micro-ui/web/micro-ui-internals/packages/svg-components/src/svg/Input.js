import React from "react";
import PropTypes from "prop-types";

export const Input = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_534)">
        <path
          d="M21 3.00977H3C1.9 3.00977 1 3.90977 1 5.00977V8.99977H3V4.98977H21V19.0198H3V14.9998H1V19.0098C1 20.1098 1.9 20.9898 3 20.9898H21C22.1 20.9898 23 20.1098 23 19.0098V5.00977C23 3.89977 22.1 3.00977 21 3.00977V3.00977ZM11 15.9998L15 11.9998L11 7.99977V10.9998H1V12.9998H11V15.9998Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_534">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



Input.propTypes = {
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

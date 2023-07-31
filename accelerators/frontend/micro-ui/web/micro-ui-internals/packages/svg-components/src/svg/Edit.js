import React from "react";
import PropTypes from "prop-types";

export const Edit = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_11379_29110)">
        <path
          d="M12.126 8.12482L14.063 6.18782L17.81 9.93482L15.873 11.8728L12.126 8.12482ZM20.71 5.62982L18.37 3.28982C18.1826 3.10357 17.9292 2.99902 17.665 2.99902C17.4008 2.99902 17.1474 3.10357 16.96 3.28982L15.13 5.11982L18.88 8.86982L20.71 6.99982C20.8844 6.81436 20.9815 6.56938 20.9815 6.31482C20.9815 6.06025 20.8844 5.81528 20.71 5.62982ZM8.63 11.6298L3 17.2498V20.9998H6.75L12.38 15.3798L15.873 11.8728L12.126 8.12482L8.63 11.6298Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_11379_29110">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



Edit.propTypes = {
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

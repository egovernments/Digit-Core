import React from "react";
import PropTypes from "prop-types";

export const MarkEmailRead = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_2129)">
        <path
          d="M12 19C12 15.13 15.13 12 19 12C20.08 12 21.09 12.25 22 12.68V6C22 4.9 21.1 4 20 4H4C2.9 4 2 4.9 2 6V18C2 19.1 2.9 20 4 20H12.08C12.03 19.67 12 19.34 12 19ZM4 6L12 11L20 6V8L12 13L4 8V6ZM17.34 22L13.8 18.46L15.21 17.05L17.33 19.17L21.57 14.93L23 16.34L17.34 22Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_2129">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



MarkEmailRead.propTypes = {
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

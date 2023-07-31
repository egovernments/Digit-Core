import React from "react";
import PropTypes from "prop-types";

export const RequestQuote = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_2467)">
        <path
          d="M14 2H6C4.9 2 4.01 2.9 4.01 4L4 20C4 21.1 4.89 22 5.99 22H18C19.1 22 20 21.1 20 20V8L14 2ZM15 12H11V13H14C14.55 13 15 13.45 15 14V17C15 17.55 14.55 18 14 18H13V19H11V18H9V16H13V15H10C9.45 15 9 14.55 9 14V11C9 10.45 9.45 10 10 10H11V9H13V10H15V12ZM13 8V3.5L17.5 8H13Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_2467">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



RequestQuote.propTypes = {
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

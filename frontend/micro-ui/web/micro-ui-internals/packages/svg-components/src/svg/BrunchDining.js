import React from "react";
import PropTypes from "prop-types";

export const BrunchDining = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_10859)">
        <path
          fill-rule="evenodd"
          clip-rule="evenodd"
          d="M18 8H20V4H18V8ZM15.51 22H2.49C2.22 22 2 21.78 2 21.5V20H16V21.5C16 21.78 15.78 22 15.51 22ZM18 15.89L17.6 15.47C16.58 14.39 16 12.95 16 11.47V2H22V11.51C22 12.97 21.46 14.38 20.47 15.45L20 15.97V20H22V22H18V15.89ZM7 16V14H11V16H15.5C15.78 16 16 16.22 16 16.5V17.5C16 17.78 15.78 18 15.5 18H2.5C2.22 18 2 17.78 2 17.5V16.5C2 16.22 2.22 16 2.5 16H7Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_10859">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



BrunchDining.propTypes = {
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

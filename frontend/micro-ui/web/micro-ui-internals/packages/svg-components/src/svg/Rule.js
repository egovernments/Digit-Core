import React from "react";
import PropTypes from "prop-types";

export const Rule = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_872)">
        <path
          d="M16.54 11.0002L13 7.46018L14.41 6.05018L16.53 8.17018L20.77 3.93018L22.18 5.34018L16.54 11.0002ZM11 7.00018H2V9.00018H11V7.00018ZM21 13.4102L19.59 12.0002L17 14.5902L14.41 12.0002L13 13.4102L15.59 16.0002L13 18.5902L14.41 20.0002L17 17.4102L19.59 20.0002L21 18.5902L18.41 16.0002L21 13.4102ZM11 15.0002H2V17.0002H11V15.0002Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_872">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



Rule.propTypes = {
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

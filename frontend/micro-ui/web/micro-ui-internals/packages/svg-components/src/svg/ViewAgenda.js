import React from "react";
import PropTypes from "prop-types";

export const ViewAgenda = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1165)">
        <path
          d="M20 13H3C2.45 13 2 13.45 2 14V20C2 20.55 2.45 21 3 21H20C20.55 21 21 20.55 21 20V14C21 13.45 20.55 13 20 13ZM20 3H3C2.45 3 2 3.45 2 4V10C2 10.55 2.45 11 3 11H20C20.55 11 21 10.55 21 10V4C21 3.45 20.55 3 20 3Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_1165">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

ViewAgenda.propTypes = {
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

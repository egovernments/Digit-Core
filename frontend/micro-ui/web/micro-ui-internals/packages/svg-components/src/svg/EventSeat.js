import React from "react";
import PropTypes from "prop-types";

export const EventSeat = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_349)">
        <path
          d="M4 18V21H7V18H17V21H20V15H4V18ZM19 10H22V13H19V10ZM2 10H5V13H2V10ZM17 13H7V5C7 3.9 7.9 3 9 3H15C16.1 3 17 3.9 17 5V13Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_349">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



EventSeat.propTypes = {
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

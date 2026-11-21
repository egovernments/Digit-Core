import React from "react";
import PropTypes from "prop-types";

export const FlightTakeoff = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_426)">
        <path
          d="M2.49984 18.9998H21.4998V20.9998H2.49984V18.9998ZM22.0698 9.63982C21.8598 8.83982 21.0298 8.35982 20.2298 8.57982L14.9198 9.99982L8.01984 3.56982L6.08984 4.07982L10.2298 11.2498L5.25984 12.5798L3.28984 11.0398L1.83984 11.4298L4.42984 15.9198C4.42984 15.9198 11.5498 14.0198 20.9998 11.4898C21.8098 11.2598 22.2798 10.4398 22.0698 9.63982Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_426">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



FlightTakeoff.propTypes = {
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

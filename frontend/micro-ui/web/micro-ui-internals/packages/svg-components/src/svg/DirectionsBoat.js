import React from "react";
import PropTypes from "prop-types";

export const DirectionsBoat = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_10932)">
        <path
          d="M20.0004 21C18.6104 21 17.2204 20.53 16.0004 19.68C13.5604 21.39 10.4404 21.39 8.00038 19.68C6.78038 20.53 5.39038 21 4.00038 21H2.00038V23H4.00038C5.38038 23 6.74038 22.65 8.00038 22.01C10.5204 23.3 13.4804 23.3 16.0004 22.01C17.2604 22.66 18.6204 23 20.0004 23H22.0004V21H20.0004ZM3.95038 19H4.00038C5.60038 19 7.02038 18.12 8.00038 17C8.98038 18.12 10.4004 19 12.0004 19C13.6004 19 15.0204 18.12 16.0004 17C16.9804 18.12 18.4004 19 20.0004 19H20.0504L21.9404 12.32C22.0204 12.06 22.0004 11.78 21.8804 11.54C21.7604 11.3 21.5404 11.12 21.2804 11.04L20.0004 10.62V6C20.0004 4.9 19.1004 4 18.0004 4H15.0004V1H9.00038V4H6.00038C4.90038 4 4.00038 4.9 4.00038 6V10.62L2.71038 11.04C2.45038 11.12 2.23038 11.3 2.11038 11.54C1.99038 11.78 1.96038 12.06 2.05038 12.32L3.95038 19ZM6.00038 6H18.0004V9.97L12.0004 8L6.00038 9.97V6Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_10932">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



DirectionsBoat.propTypes = {
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

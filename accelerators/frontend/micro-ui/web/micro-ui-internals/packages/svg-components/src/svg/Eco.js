import React from "react";
import PropTypes from "prop-types";

export const Eco = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_331)">
        <path
          d="M6.05001 8.05C3.32001 10.78 3.32001 15.2 6.03001 17.93C7.50001 14.53 10.12 11.69 13.39 10C10.62 12.34 8.68001 15.61 8.00001 19.32C10.6 20.55 13.8 20.1 15.95 17.95C19.43 14.47 20 4 20 4C20 4 9.53001 4.57 6.05001 8.05Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_331">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



Eco.propTypes = {
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

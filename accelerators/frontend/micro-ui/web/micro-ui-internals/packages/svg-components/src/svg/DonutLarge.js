import React from "react";
import PropTypes from "prop-types";

export const DonutLarge = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_314)">
        <path
          d="M11 5.08V2C6 2.5 2 6.81 2 12C2 17.19 6 21.5 11 22V18.92C8 18.44 5 15.52 5 12C5 8.48 8 5.56 11 5.08ZM18.97 11H22C21.53 6 18 2.47 13 2V5.08C16 5.51 18.54 8 18.97 11ZM13 18.92V22C18 21.53 21.53 18 22 13H18.97C18.54 16 16 18.49 13 18.92Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_314">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



DonutLarge.propTypes = {
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

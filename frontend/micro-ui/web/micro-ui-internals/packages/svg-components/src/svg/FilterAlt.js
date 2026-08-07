import React from "react";
import PropTypes from "prop-types";

export const FilterAlt = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_393)">
        <path
          d="M4.24969 5.61C6.26969 8.2 9.99969 13 9.99969 13V19C9.99969 19.55 10.4497 20 10.9997 20H12.9997C13.5497 20 13.9997 19.55 13.9997 19V13C13.9997 13 17.7197 8.2 19.7397 5.61C20.2497 4.95 19.7797 4 18.9497 4H5.03969C4.20969 4 3.73969 4.95 4.24969 5.61Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_393">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



FilterAlt.propTypes = {
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

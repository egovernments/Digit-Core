import React from "react";
import PropTypes from "prop-types";

export const SystemUpdateAlt = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1043)">
        <path
          d="M12 16.5L16 12.5H13V3.5H11V12.5H8L12 16.5ZM21 3.5H15V5.49H21V19.52H3V5.49H9V3.5H3C1.9 3.5 1 4.4 1 5.5V19.5C1 20.6 1.9 21.5 3 21.5H21C22.1 21.5 23 20.6 23 19.5V5.5C23 4.4 22.1 3.5 21 3.5Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_1043">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

SystemUpdateAlt.propTypes = {
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

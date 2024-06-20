import React from "react";
import PropTypes from "prop-types";

export const TabUnselected = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1053)">
        <path
          d="M1 9H3V7H1V9ZM1 13H3V11H1V13ZM1 5H3V3C1.9 3 1 3.9 1 5ZM9 21H11V19H9V21ZM1 17H3V15H1V17ZM3 21V19H1C1 20.1 1.9 21 3 21ZM21 3H13V9H23V5C23 3.9 22.1 3 21 3ZM21 17H23V15H21V17ZM9 5H11V3H9V5ZM5 21H7V19H5V21ZM5 5H7V3H5V5ZM21 21C22.1 21 23 20.1 23 19H21V21ZM21 13H23V11H21V13ZM13 21H15V19H13V21ZM17 21H19V19H17V21Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_1053">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

TabUnselected.propTypes = {
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

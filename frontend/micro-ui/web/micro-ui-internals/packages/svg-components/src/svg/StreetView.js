import React from "react";
import PropTypes from "prop-types";

export const StreetView = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11367)">
        <path
          d="M12.56 14.33C12.22 14.6 12 15.03 12 15.5V21H19C20.1 21 21 20.1 21 19V13.02C20.06 12.69 19.05 12.5 18 12.5C15.97 12.5 14.07 13.2 12.56 14.33V14.33Z"
          fill={fill}
        />
        <path d="M18 11C20.7614 11 23 8.76142 23 6C23 3.23858 20.7614 1 18 1C15.2386 1 13 3.23858 13 6C13 8.76142 15.2386 11 18 11Z" fill={fill} />
        <path
          d="M11.5 6C11.5 4.92 11.77 3.9 12.24 3H5C3.9 3 3 3.9 3 5V19C3 19.55 3.23 20.05 3.59 20.41L13.41 10.59C12.23 9.42 11.5 7.8 11.5 6Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_11367">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



StreetView.propTypes = {
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

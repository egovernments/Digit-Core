import React from "react";
import PropTypes from "prop-types";

export const Unsubscribe = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <path
        d="M18.5 13C16.57 13 15 14.57 15 16.5C15 18.43 16.57 20 18.5 20C20.43 20 22 18.43 22 16.5C22 14.57 20.43 13 18.5 13ZM20.5 17H16.5V16H20.5V17ZM13.55 17C13.53 16.83 13.5 16.67 13.5 16.5C13.5 13.74 15.74 11.5 18.5 11.5C19.42 11.5 20.26 11.76 21 12.19V5C21 3.9 20.1 3 19 3H5C3.9 3 3 3.9 3 5V15C3 16.1 3.9 17 5 17H13.55ZM12 10.5L5 7V5L12 8.5L19 5V7L12 10.5Z"
        fill={fill}
      />
    </svg>
  );
};

Unsubscribe.propTypes = {
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

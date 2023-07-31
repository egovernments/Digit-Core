import React from "react";
import PropTypes from "prop-types";

export const Clock = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <path
        fill-rule="evenodd"
        clip-rule="evenodd"
        d="M11.988 0C5.364 0 0 5.376 0 12C0 18.624 5.364 24 11.988 24C18.624 24 24 18.624 24 12C24 5.376 18.624 0 11.988 0ZM11.9999 21.6C6.6959 21.6 2.3999 17.304 2.3999 12C2.3999 6.696 6.6959 2.4 11.9999 2.4C17.3039 2.4 21.5999 6.696 21.5999 12C21.5999 17.304 17.3039 21.6 11.9999 21.6ZM10.8 6H12.6V12.3L18 15.504L17.1 16.98L10.8 13.2V6Z"
        fill={fill}
      />
    </svg>
  );
};



Clock.propTypes = {
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

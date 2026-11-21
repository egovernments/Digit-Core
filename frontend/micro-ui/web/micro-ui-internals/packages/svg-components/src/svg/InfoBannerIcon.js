import React from "react";
import PropTypes from "prop-types";

export const InfoBannerIcon = ({ className, height = "24", width = "24", style = {}, fill = "#3498DB", onClick = null }) => {
  return (
    <svg xmlns="http://www.w3.org/2000/svg" width={width} height={height} className={className} onClick={onClick} viewBox="0 0 20 20" fill="none">
      <path
        d="M10 0C4.48 0 0 4.48 0 10C0 15.52 4.48 20 10 20C15.52 20 20 15.52 20 10C20 4.48 15.52 0 10 0ZM11 15H9V9H11V15ZM11 7H9V5H11V7Z"
        fill={fill}
      />
    </svg>
  );
};

InfoBannerIcon.propTypes = {
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

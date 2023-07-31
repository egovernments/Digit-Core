import React from "react";
import PropTypes from "prop-types";

export const Deck = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_176_613)">
        <path d="M22 9L12 2L2 9H11V22H13V9H22Z" fill={fill} />
        <path d="M4.13969 12L2.17969 12.37L2.99969 16.74V22H4.99969L5.01969 18H6.99969V22H8.99969V16H4.89969L4.13969 12Z" fill={fill} />
        <path d="M19.1 16H15V22H17V18H18.98L19 22H21V16.74L21.82 12.37L19.86 12L19.1 16Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_176_613">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



Deck.propTypes = {
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

import React from "react";
import PropTypes from "prop-types";

export const EmojiPeople = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_176_660)">
        <path d="M12 6C13.1046 6 14 5.10457 14 4C14 2.89543 13.1046 2 12 2C10.8954 2 10 2.89543 10 4C10 5.10457 10.8954 6 12 6Z" fill={fill} />
        <path
          d="M15.89 8.11C15.5 7.72 14.83 7 13.53 7C13.32 7 12.11 7 10.99 7C8.24 6.99 6 4.75 6 2H4C4 5.16 6.11 7.84 9 8.71V22H11V16H13V22H15V10.05L18.95 14L20.36 12.59L15.89 8.11Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_176_660">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



EmojiPeople.propTypes = {
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

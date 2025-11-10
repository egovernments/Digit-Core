import React from "react";
import PropTypes from "prop-types";

export const EmojiSymbols = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_176_667)">
        <path d="M11 2H3V4H11V2Z" fill={fill} />
        <path d="M6 11H8V7H11V5H3V7H6V11Z" fill={fill} />
        <path d="M20.1824 12.4038L12.4043 20.1819L13.8185 21.5961L21.5966 13.818L20.1824 12.4038Z" fill={fill} />
        <path
          d="M14.5 16C15.3284 16 16 15.3284 16 14.5C16 13.6716 15.3284 13 14.5 13C13.6716 13 13 13.6716 13 14.5C13 15.3284 13.6716 16 14.5 16Z"
          fill={fill}
        />
        <path
          d="M19.5 21C20.3284 21 21 20.3284 21 19.5C21 18.6716 20.3284 18 19.5 18C18.6716 18 18 18.6716 18 19.5C18 20.3284 18.6716 21 19.5 21Z"
          fill={fill}
        />
        <path
          d="M15.5 11C16.88 11 18 9.88 18 8.5V4H21V2H17V6.51C16.58 6.19 16.07 6 15.5 6C14.12 6 13 7.12 13 8.5C13 9.88 14.12 11 15.5 11Z"
          fill={fill}
        />
        <path
          d="M9.74035 15.96L8.33035 17.37L7.62035 16.66L7.97035 16.31C8.95035 15.33 8.95035 13.75 7.97035 12.77C7.48035 12.28 6.84035 12.04 6.20035 12.04C5.56035 12.04 4.92035 12.28 4.43035 12.77C3.45035 13.75 3.45035 15.33 4.43035 16.31L4.78035 16.66L3.72035 17.72C2.74035 18.7 2.74035 20.28 3.72035 21.26C4.22035 21.76 4.86035 22 5.50035 22C6.14035 22 6.78035 21.76 7.27035 21.27L8.33035 20.21L9.74035 21.62L11.1504 20.21L9.74035 18.8L11.1504 17.39L9.74035 15.96ZM5.85035 14.2C5.97035 14.08 6.11035 14.05 6.20035 14.05C6.29035 14.05 6.43035 14.08 6.55035 14.2C6.74035 14.4 6.74035 14.71 6.55035 14.91L6.20035 15.26L5.85035 14.9C5.66035 14.71 5.66035 14.39 5.85035 14.2ZM5.85035 19.85C5.73035 19.97 5.59035 20 5.50035 20C5.41035 20 5.27035 19.97 5.15035 19.85C4.96035 19.66 4.96035 19.34 5.15035 19.14L6.21035 18.08L6.92035 18.79L5.85035 19.85Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_176_667">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



EmojiSymbols.propTypes = {
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

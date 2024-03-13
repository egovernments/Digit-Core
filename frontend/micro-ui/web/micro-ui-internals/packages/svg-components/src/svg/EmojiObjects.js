import React from "react";
import PropTypes from "prop-types";

export const EmojiObjects = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_176_655)">
        <path
          d="M12.0003 3C11.5403 3 11.0703 3.04 10.6003 3.14C7.84032 3.67 5.64032 5.9 5.12032 8.66C4.64032 11.27 5.60032 13.67 7.34032 15.22C7.77032 15.6 8.00032 16.13 8.00032 16.69V19C8.00032 20.1 8.90032 21 10.0003 21H10.2803C10.6303 21.6 11.2603 22 12.0003 22C12.7403 22 13.3803 21.6 13.7203 21H14.0003C15.1003 21 16.0003 20.1 16.0003 19V16.69C16.0003 16.14 16.2203 15.6 16.6403 15.23C18.0903 13.95 19.0003 12.08 19.0003 10C19.0003 6.13 15.8703 3 12.0003 3ZM14.0003 19H10.0003V18H14.0003V19ZM14.0003 17H10.0003V16H14.0003V17ZM12.5003 11.41V14H11.5003V11.41L9.67032 9.59L10.3803 8.88L12.0003 10.5L13.6203 8.88L14.3303 9.59L12.5003 11.41Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_176_655">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



EmojiObjects.propTypes = {
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

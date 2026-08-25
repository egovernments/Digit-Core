import React from "react";
import PropTypes from "prop-types";

export const DirectionsRun = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_10944)">
        <path
          d="M13.4896 5.47998C14.5896 5.47998 15.4896 4.57998 15.4896 3.47998C15.4896 2.37998 14.5896 1.47998 13.4896 1.47998C12.3896 1.47998 11.4896 2.37998 11.4896 3.47998C11.4896 4.57998 12.3896 5.47998 13.4896 5.47998ZM9.88965 19.38L10.8896 14.98L12.9896 16.98V22.98H14.9896V15.48L12.8896 13.48L13.4896 10.48C14.7896 11.98 16.7896 12.98 18.9896 12.98V10.98C17.0896 10.98 15.4896 9.97998 14.6896 8.57998L13.6896 6.97998C13.2896 6.37998 12.6896 5.97998 11.9896 5.97998C11.6896 5.97998 11.4896 6.07998 11.1896 6.07998L5.98965 8.27998V12.98H7.98965V9.57998L9.78965 8.87998L8.18965 16.98L3.28965 15.98L2.88965 17.98L9.88965 19.38Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_10944">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



DirectionsRun.propTypes = {
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

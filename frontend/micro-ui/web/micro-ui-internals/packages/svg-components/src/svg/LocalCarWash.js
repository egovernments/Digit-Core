import React from "react";
import PropTypes from "prop-types";

export const LocalCarWash = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11119)">
        <path
          d="M17 4.9998C17.83 4.9998 18.5 4.3298 18.5 3.4998C18.5 2.4998 17 0.799805 17 0.799805C17 0.799805 15.5 2.4998 15.5 3.4998C15.5 4.3298 16.17 4.9998 17 4.9998ZM12 4.9998C12.83 4.9998 13.5 4.3298 13.5 3.4998C13.5 2.4998 12 0.799805 12 0.799805C12 0.799805 10.5 2.4998 10.5 3.4998C10.5 4.3298 11.17 4.9998 12 4.9998ZM7 4.9998C7.83 4.9998 8.5 4.3298 8.5 3.4998C8.5 2.4998 7 0.799805 7 0.799805C7 0.799805 5.5 2.4998 5.5 3.4998C5.5 4.3298 6.17 4.9998 7 4.9998ZM18.92 8.0098C18.72 7.4198 18.16 6.9998 17.5 6.9998H6.5C5.84 6.9998 5.29 7.4198 5.08 8.0098L3 13.9998V21.9998C3 22.5498 3.45 22.9998 4 22.9998H5C5.55 22.9998 6 22.5498 6 21.9998V20.9998H18V21.9998C18 22.5498 18.45 22.9998 19 22.9998H20C20.55 22.9998 21 22.5498 21 21.9998V13.9998L18.92 8.0098ZM6.5 17.9998C5.67 17.9998 5 17.3298 5 16.4998C5 15.6698 5.67 14.9998 6.5 14.9998C7.33 14.9998 8 15.6698 8 16.4998C8 17.3298 7.33 17.9998 6.5 17.9998ZM17.5 17.9998C16.67 17.9998 16 17.3298 16 16.4998C16 15.6698 16.67 14.9998 17.5 14.9998C18.33 14.9998 19 15.6698 19 16.4998C19 17.3298 18.33 17.9998 17.5 17.9998ZM5 12.9998L6.5 8.4998H17.5L19 12.9998H5Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_11119">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



LocalCarWash.propTypes = {
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

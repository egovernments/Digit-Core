import React from "react";
import PropTypes from "prop-types";

export const LocalFlorist = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11136)">
        <path
          d="M12 22C16.97 22 21 17.97 21 13C16.03 13 12 17.03 12 22ZM5.6 10.25C5.6 11.63 6.72 12.75 8.1 12.75C8.63 12.75 9.11 12.59 9.52 12.31L9.5 12.5C9.5 13.88 10.62 15 12 15C13.38 15 14.5 13.88 14.5 12.5L14.48 12.31C14.88 12.59 15.37 12.75 15.9 12.75C17.28 12.75 18.4 11.63 18.4 10.25C18.4 9.25 17.81 8.4 16.97 8C17.81 7.6 18.4 6.75 18.4 5.75C18.4 4.37 17.28 3.25 15.9 3.25C15.37 3.25 14.89 3.41 14.48 3.69L14.5 3.5C14.5 2.12 13.38 1 12 1C10.62 1 9.5 2.12 9.5 3.5L9.52 3.69C9.12 3.41 8.63 3.25 8.1 3.25C6.72 3.25 5.6 4.37 5.6 5.75C5.6 6.75 6.19 7.6 7.03 8C6.19 8.4 5.6 9.25 5.6 10.25ZM12 5.5C13.38 5.5 14.5 6.62 14.5 8C14.5 9.38 13.38 10.5 12 10.5C10.62 10.5 9.5 9.38 9.5 8C9.5 6.62 10.62 5.5 12 5.5ZM3 13C3 17.97 7.03 22 12 22C12 17.03 7.97 13 3 13Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_11136">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



LocalFlorist.propTypes = {
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

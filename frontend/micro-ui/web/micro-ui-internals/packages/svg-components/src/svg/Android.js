import React from "react";
import PropTypes from "prop-types";

export const Android = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_83)">
        <path
          d="M17.6 9.47997L19.44 6.29997C19.6 5.98997 19.48 5.60997 19.18 5.44997C18.89 5.29997 18.53 5.38997 18.35 5.66997L16.47 8.90997C13.61 7.69997 10.39 7.69997 7.53 8.90997L5.65 5.66997C5.46 5.37997 5.07 5.28997 4.78 5.46997C4.5 5.64997 4.41 6.00997 4.56 6.29997L6.4 9.47997C3.3 11.25 1.28 14.44 1 18H23C22.72 14.44 20.7 11.25 17.6 9.47997ZM7 15.25C6.31 15.25 5.75 14.69 5.75 14C5.75 13.31 6.31 12.75 7 12.75C7.69 12.75 8.25 13.31 8.25 14C8.25 14.69 7.69 15.25 7 15.25ZM17 15.25C16.31 15.25 15.75 14.69 15.75 14C15.75 13.31 16.31 12.75 17 12.75C17.69 12.75 18.25 13.31 18.25 14C18.25 14.69 17.69 15.25 17 15.25Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_83">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

Android.propTypes = {
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

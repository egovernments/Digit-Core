import React from "react";
import PropTypes from "prop-types";

export const OutdoorGrill = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_176_784)">
        <path
          d="M17 22C18.66 22 20 20.66 20 19C20 17.34 18.66 16 17 16C15.7 16 14.6 16.84 14.18 18H9.14L11.13 14.94C11.42 14.98 11.71 15 12 15C12.29 15 12.58 14.98 12.87 14.94L13.89 16.51C14.31 15.98 14.85 15.56 15.49 15.3L14.89 14.37C17.31 13.27 19 10.84 19 8H5C5 10.84 6.69 13.27 9.12 14.37L5.17 20.45C4.87 20.91 5 21.53 5.46 21.83C5.92 22.13 6.54 22 6.84 21.54L7.84 19.99H14.18C14.6 21.16 15.7 22 17 22ZM17 18C17.55 18 18 18.45 18 19C18 19.55 17.55 20 17 20C16.45 20 16 19.55 16 19C16 18.45 16.45 18 17 18Z"
          fill={fill}
        />
        <path
          d="M9.40952 7H10.4095C10.5595 5.85 10.6395 5.36 9.51952 4.04C9.09952 3.54 8.83952 3.27 9.05952 2H8.06952C7.85952 3.11 8.09952 4.05 8.95952 4.96C9.17952 5.2 9.74952 5.63 9.40952 7Z"
          fill={fill}
        />
        <path
          d="M11.89 7H12.89C13.04 5.85 13.12 5.36 12 4.04C11.58 3.54 11.32 3.26 11.54 2H10.55C10.34 3.11 10.58 4.05 11.44 4.96C11.67 5.2 12.24 5.63 11.89 7Z"
          fill={fill}
        />
        <path
          d="M14.4095 7H15.4095C15.5595 5.85 15.6395 5.36 14.5195 4.04C14.0995 3.54 13.8395 3.27 14.0595 2H13.0695C12.8595 3.11 13.0995 4.05 13.9595 4.96C14.1795 5.2 14.7495 5.63 14.4095 7Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_176_784">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



OutdoorGrill.propTypes = {
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

import React from "react";
import PropTypes from "prop-types";

export const LocalDining = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11125)">
        <path
          d="M8.10023 13.34L10.9302 10.51L3.91023 3.49996C2.35023 5.05996 2.35023 7.58996 3.91023 9.15996L8.10023 13.34V13.34ZM14.8802 11.53C16.4102 12.24 18.5602 11.74 20.1502 10.15C22.0602 8.23996 22.4302 5.49996 20.9602 4.02996C19.5002 2.56996 16.7602 2.92996 14.8402 4.83996C13.2502 6.42996 12.7502 8.57996 13.4602 10.11L3.70023 19.87L5.11023 21.28L12.0002 14.41L18.8802 21.29L20.2902 19.88L13.4102 13L14.8802 11.53V11.53Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_11125">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



LocalDining.propTypes = {
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

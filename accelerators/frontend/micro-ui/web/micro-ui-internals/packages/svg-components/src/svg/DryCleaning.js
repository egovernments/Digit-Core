import React from "react";
import PropTypes from "prop-types";

export const DryCleaning = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_10956)">
        <path
          d="M19.56 11.3602L13 8.44018V7.00018C13 6.45018 12.55 6.00018 12 6.00018C11.45 6.00018 11 5.55018 11 5.00018C11 4.45018 11.45 4.00018 12 4.00018C12.55 4.00018 13 4.45018 13 5.00018H15C15 3.16018 13.34 1.70018 11.44 2.05018C10.26 2.27018 9.29 3.22018 9.06 4.40018C8.76 5.96018 9.66 7.34018 11 7.82018V8.45018L4.44 11.3702C3.56 11.7502 3 12.6202 3 13.5702V13.5802C3 14.9202 4.08 16.0002 5.42 16.0002H7V22.0002H17V16.0002H18.58C19.92 16.0002 21 14.9202 21 13.5802V13.5702C21 12.6202 20.44 11.7502 19.56 11.3602ZM18.58 14.0002H17V13.0002H7V14.0002H5.42C5.19 14.0002 5 13.8102 5 13.5702C5 13.4002 5.1 13.2502 5.25 13.1902L12 10.1902L18.75 13.1902C18.9 13.2602 19 13.4102 19 13.5802C19 13.8102 18.81 14.0002 18.58 14.0002Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_10956">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



DryCleaning.propTypes = {
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

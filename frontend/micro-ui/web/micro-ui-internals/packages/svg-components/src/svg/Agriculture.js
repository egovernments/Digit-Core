import React from "react";
import PropTypes from "prop-types";

export const Agriculture = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_10812)">
        <path
          d="M19.5003 11.9998C20.4303 11.9998 21.2803 12.2798 22.0003 12.7598V7.99982C22.0003 6.89982 21.1003 5.99982 20.0003 5.99982H13.7103L12.6503 4.93982L14.0603 3.52982L13.3503 2.81982L9.82031 6.34982L10.5303 7.05982L11.9403 5.64982L13.0003 6.70982V8.99982C13.0003 10.0998 12.1003 10.9998 11.0003 10.9998H10.4603C11.4103 12.0598 12.0003 13.4598 12.0003 14.9998C12.0003 15.3398 11.9603 15.6698 11.9103 15.9998H15.0503C15.3003 13.7498 17.1903 11.9998 19.5003 11.9998Z"
          fill={fill}
        />
        <path
          d="M19.5 13C17.57 13 16 14.57 16 16.5C16 18.43 17.57 20 19.5 20C21.43 20 23 18.43 23 16.5C23 14.57 21.43 13 19.5 13ZM19.5 18C18.67 18 18 17.33 18 16.5C18 15.67 18.67 15 19.5 15C20.33 15 21 15.67 21 16.5C21 17.33 20.33 18 19.5 18Z"
          fill={fill}
        />
        <path d="M4 9H9C9 7.9 8.1 7 7 7H4C3.45 7 3 7.45 3 8C3 8.55 3.45 9 4 9Z" fill={fill} />
        <path
          d="M9.83 13.82L9.65 13.35L10.58 13C10.12 11.94 9.3 11.09 8.27 10.57L7.87 11.46L7.41 11.25L7.81 10.35C7.26 10.13 6.64 10 6 10C5.47 10 4.96 10.11 4.48 10.26L4.82 11.17L4.35 11.35L4 10.42C2.94 10.88 2.09 11.7 1.57 12.73L2.46 13.13L2.25 13.59L1.35 13.19C1.13 13.74 1 14.36 1 15C1 15.53 1.11 16.04 1.26 16.52L2.17 16.18L2.35 16.65L1.42 17C1.88 18.06 2.7 18.91 3.73 19.43L4.13 18.54L4.59 18.75L4.19 19.65C4.74 19.87 5.36 20 6 20C6.53 20 7.04 19.89 7.52 19.74L7.18 18.83L7.65 18.65L8 19.58C9.06 19.12 9.91 18.3 10.43 17.27L9.54 16.87L9.75 16.41L10.65 16.81C10.87 16.26 11 15.64 11 15C11 14.47 10.89 13.96 10.74 13.48L9.83 13.82ZM7.15 17.77C5.62 18.4 3.86 17.68 3.23 16.15C2.6 14.62 3.32 12.86 4.85 12.23C6.38 11.6 8.14 12.32 8.77 13.85C9.41 15.38 8.68 17.14 7.15 17.77Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_10812">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

Agriculture.propTypes = {
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

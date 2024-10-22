import React from "react";
import PropTypes from "prop-types";

export const NoLuggage = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_176_781)">
        <path
          d="M12.7496 9V9.92L14.4996 11.67V9H15.9996V13.17L18.9996 16.17V8C18.9996 6.9 18.0996 6 16.9996 6H14.9996V3C14.9996 2.45 14.5496 2 13.9996 2H9.99965C9.44965 2 8.99965 2.45 8.99965 3V6H8.82965L11.8296 9H12.7496ZM10.4996 3.5H13.4996V6H10.4996V3.5ZM21.1896 21.19L2.80965 2.81L1.38965 4.22L5.01965 7.85C5.01965 7.9 4.99965 7.95 4.99965 8V19C4.99965 20.1 5.89965 21 6.99965 21C6.99965 21.55 7.44965 22 7.99965 22C8.54965 22 8.99965 21.55 8.99965 21H14.9996C14.9996 21.55 15.4496 22 15.9996 22C16.5496 22 16.9996 21.55 16.9996 21C17.3396 21 17.6496 20.91 17.9296 20.76L19.7796 22.61L21.1896 21.19ZM7.99965 18V10.83L9.49965 12.33V18H7.99965ZM12.7496 18H11.2496V14.08L12.7496 15.58V18Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_176_781">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



NoLuggage.propTypes = {
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

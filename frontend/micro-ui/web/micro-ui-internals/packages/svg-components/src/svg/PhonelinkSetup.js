import React from "react";
import PropTypes from "prop-types";

export const PhonelinkSetup = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_2190)">
        <path
          d="M10.8198 12.49C10.8398 12.33 10.8598 12.17 10.8598 12C10.8598 11.83 10.8398 11.67 10.8198 11.51L11.8998 10.69C11.9998 10.62 12.0198 10.48 11.9598 10.37L10.9298 8.64C10.8698 8.53 10.7298 8.49 10.6198 8.53L9.33981 9.03C9.06981 8.83 8.77981 8.67 8.46981 8.54L8.26981 7.21C8.26981 7.09 8.15981 7 8.02981 7H5.97981C5.84981 7 5.73981 7.09 5.71981 7.21L5.51981 8.53C5.20981 8.65 4.91981 8.83 4.64981 9.02L3.36981 8.52C3.24981 8.47 3.11981 8.52 3.05981 8.63L2.02981 10.36C1.96981 10.48 1.99981 10.61 2.09981 10.69L3.17981 11.51C3.15981 11.67 3.14981 11.84 3.14981 12C3.14981 12.17 3.16981 12.33 3.18981 12.49L2.09981 13.32C1.99981 13.39 1.97981 13.53 2.03981 13.64L3.06981 15.37C3.12981 15.48 3.26981 15.52 3.37981 15.48L4.65981 14.98C4.92981 15.18 5.21981 15.34 5.52981 15.47L5.72981 16.79C5.73981 16.91 5.84981 17 5.97981 17H8.03981C8.16981 17 8.27981 16.91 8.28981 16.79L8.48981 15.47C8.79981 15.35 9.08981 15.17 9.35981 14.98L10.6398 15.48C10.7598 15.53 10.8898 15.48 10.9498 15.37L11.9798 13.64C12.0398 13.53 12.0198 13.4 11.9198 13.32L10.8198 12.49V12.49ZM6.99981 13.75C6.00981 13.75 5.19981 12.97 5.19981 12C5.19981 11.03 6.00981 10.25 6.99981 10.25C7.98981 10.25 8.79981 11.03 8.79981 12C8.79981 12.97 7.99981 13.75 6.99981 13.75ZM17.9998 1.01L7.99981 1C6.89981 1 5.99981 1.9 5.99981 3V6H7.99981V5H17.9998V19H7.99981V18H5.99981V21C5.99981 22.1 6.89981 23 7.99981 23H17.9998C19.0998 23 19.9998 22.1 19.9998 21V3C19.9998 1.9 19.0998 1.01 17.9998 1.01Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_2190">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



PhonelinkSetup.propTypes = {
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

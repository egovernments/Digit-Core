import React from "react";
import PropTypes from "prop-types";

export const AlarmOff = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_64)">
        <path
          d="M12.0004 6.00011C15.8704 6.00011 19.0004 9.13011 19.0004 13.0001C19.0004 13.8401 18.8404 14.6501 18.5704 15.4001L20.0904 16.9201C20.6704 15.7301 21.0004 14.4101 21.0004 13.0001C21.0004 8.03011 16.9704 4.00011 12.0004 4.00011C10.5904 4.00011 9.27039 4.33011 8.08039 4.91011L9.60039 6.43011C10.3504 6.16011 11.1604 6.00011 12.0004 6.00011ZM22.0004 5.72011L17.4004 1.86011L16.1104 3.39011L20.7104 7.25011L22.0004 5.72011ZM2.92039 2.29011L1.65039 3.57011L2.98039 4.90011L1.87039 5.83011L3.29039 7.25011L4.40039 6.31011L5.20039 7.11011C3.83039 8.69011 3.00039 10.7501 3.00039 13.0001C3.00039 17.9701 7.02039 22.0001 12.0004 22.0001C14.2504 22.0001 16.3104 21.1701 17.8904 19.8001L20.0904 22.0001L21.3604 20.7301L3.89039 3.27011L2.92039 2.29011ZM16.4704 18.3901C15.2604 19.3901 13.7004 20.0001 12.0004 20.0001C8.13039 20.0001 5.00039 16.8701 5.00039 13.0001C5.00039 11.3001 5.61039 9.74011 6.61039 8.53011L16.4704 18.3901ZM8.02039 3.28011L6.60039 1.86011L5.74039 2.57011L7.16039 3.99011L8.02039 3.28011V3.28011Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_64">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

AlarmOff.propTypes = {
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

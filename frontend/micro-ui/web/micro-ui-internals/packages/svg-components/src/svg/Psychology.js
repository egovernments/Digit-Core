import React from "react";
import PropTypes from "prop-types";

export const Psychology = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_176_853)">
        <path
          d="M13.0003 8.57007C12.2103 8.57007 11.5703 9.21007 11.5703 10.0001C11.5703 10.7901 12.2103 11.4301 13.0003 11.4301C13.7903 11.4301 14.4303 10.7901 14.4303 10.0001C14.4303 9.21007 13.7903 8.57007 13.0003 8.57007Z"
          fill={fill}
        />
        <path
          d="M12.9998 3C9.24982 3 6.19982 5.94 6.01982 9.64L4.09982 12.2C3.84982 12.53 4.08982 13 4.49982 13H5.99982V16C5.99982 17.1 6.89982 18 7.99982 18H8.99982V21H15.9998V16.32C18.3598 15.2 19.9998 12.79 19.9998 10C19.9998 6.13 16.8698 3 12.9998 3ZM15.9998 10C15.9998 10.13 15.9898 10.26 15.9798 10.39L16.8098 11.05C16.8898 11.11 16.9098 11.21 16.8598 11.3L16.0598 12.69C16.0098 12.78 15.8998 12.81 15.8198 12.78L14.8298 12.38C14.6198 12.54 14.3998 12.67 14.1598 12.77L13.9998 13.83C13.9898 13.93 13.8998 14 13.7998 14H12.1998C12.0998 14 12.0198 13.93 11.9998 13.83L11.8498 12.77C11.5998 12.67 11.3798 12.54 11.1698 12.38L10.1798 12.78C10.0898 12.81 9.97982 12.78 9.92982 12.69L9.12982 11.3C9.07982 11.22 9.09982 11.11 9.17982 11.05L10.0198 10.39C10.0098 10.26 9.99982 10.13 9.99982 10C9.99982 9.87 10.0198 9.73 10.0398 9.61L9.18982 8.95C9.10982 8.89 9.08982 8.79 9.13982 8.69L9.93982 7.31C9.98982 7.22 10.0898 7.19 10.1798 7.22L11.1798 7.62C11.3798 7.47 11.6098 7.33 11.8498 7.23L11.9998 6.17C12.0198 6.07 12.0998 6 12.1998 6H13.7998C13.8998 6 13.9798 6.07 13.9998 6.17L14.1498 7.23C14.3898 7.33 14.6098 7.46 14.8198 7.62L15.8198 7.22C15.9098 7.19 16.0198 7.22 16.0598 7.31L16.8598 8.69C16.9098 8.78 16.8898 8.89 16.8098 8.95L15.9598 9.61C15.9898 9.73 15.9998 9.86 15.9998 10Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_176_853">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



Psychology.propTypes = {
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

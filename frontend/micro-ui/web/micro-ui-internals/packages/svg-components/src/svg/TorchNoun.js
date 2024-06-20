import React from "react";
import PropTypes from "prop-types";

export const TorchNoun = ({ className, style = {}, height = "20", width = "25", fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 20 25" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_55046_131540)">
        <path d="M14.6 0H5.4C5.2 0 5 0.2 5 0.4V1.8C5 1.9 5.1 2 5.2 2H14.8C14.9 2 15 1.9 15 1.8V0.4C15 0.2 14.8 0 14.6 0Z" fill={fill} />
        <path d="M12.8 18H7.2C7.1 18 7 18.1 7 18.2V19C7 19.6 7.4 20 8 20H12C12.6 20 13 19.6 13 19V18.2C13 18.1 12.9 18 12.8 18Z" fill={fill} />
        <path
          d="M14.1996 3H5.79961C5.69961 3 5.59961 3.2 5.59961 3.3L6.99961 5.9C6.99961 6 6.99961 6 6.99961 6.1V16.8C6.99961 16.9 7.09961 17 7.19961 17H12.7996C12.8996 17 12.9996 16.9 12.9996 16.8V6.1C12.9996 6 12.9996 6 12.9996 5.9L14.2996 3.3C14.3996 3.2 14.2996 3 14.1996 3ZM9.99961 9C9.39961 9 8.99961 8.6 8.99961 8C8.99961 7.4 9.39961 7 9.99961 7C10.5996 7 10.9996 7.4 10.9996 8C10.9996 8.6 10.5996 9 9.99961 9Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_55046_131540">
          <rect width="20" height="25" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

TorchNoun.propTypes = {
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

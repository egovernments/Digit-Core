import React from "react";
import PropTypes from "prop-types";

export const TextRotationAngleDown = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1062)">
        <path
          d="M19.3996 4.9101L18.3396 3.8501L7.19961 8.2701L8.67961 9.7501L10.8696 8.8301L14.4096 12.3701L13.4896 14.5601L14.9696 16.0401L19.3996 4.9101ZM12.5896 8.0101L17.4596 5.7801L15.2296 10.6501L12.5896 8.0101V8.0101ZM14.2696 21.0001V16.7601L12.8596 18.1701L4.01961 9.3301L2.59961 10.7501L11.4396 19.5901L10.0296 21.0001H14.2696Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_1062">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

TextRotationAngleDown.propTypes = {
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

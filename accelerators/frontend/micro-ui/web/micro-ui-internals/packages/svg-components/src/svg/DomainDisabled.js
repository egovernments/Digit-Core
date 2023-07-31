import React from "react";
import PropTypes from "prop-types";

export const DomainDisabled = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_2060)">
        <path
          d="M7.99961 4.9998H9.99961V6.9998H9.09961L11.9996 9.8998V8.9998H19.9996V17.8998L21.9996 19.8998V6.9998H11.9996V2.9998H5.09961L7.99961 5.89981V4.9998ZM15.9996 10.9998H17.9996V12.9998H15.9996V10.9998ZM1.29961 1.7998L0.0996094 3.0998L1.99961 4.9998V20.9998H17.9996L20.9996 23.9998L22.2996 22.6998L1.29961 1.7998V1.7998ZM5.99961 18.9998H3.99961V16.9998H5.99961V18.9998ZM5.99961 14.9998H3.99961V12.9998H5.99961V14.9998ZM5.99961 10.9998H3.99961V8.9998H5.99961V10.9998ZM9.99961 18.9998H7.99961V16.9998H9.99961V18.9998ZM9.99961 14.9998H7.99961V12.9998H9.99961V14.9998ZM11.9996 18.9998V16.9998H13.9996L15.9996 18.9998H11.9996Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_2060">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



DomainDisabled.propTypes = {
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

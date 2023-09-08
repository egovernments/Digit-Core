import React from "react";
import PropTypes from "prop-types";

export const NearMeDisabled = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11271)">
        <path
          d="M11.9996 6.34014L20.9996 3.00014L17.6596 12.0001L11.9996 6.34014ZM22.6096 19.7801L4.21957 1.39014L2.80957 2.81014L7.87957 7.88014L2.99957 9.69014V11.1001L10.0696 13.9301L12.8996 21.0001H14.3096L16.1196 16.1201L21.1896 21.1901L22.6096 19.7801Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_11271">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



NearMeDisabled.propTypes = {
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

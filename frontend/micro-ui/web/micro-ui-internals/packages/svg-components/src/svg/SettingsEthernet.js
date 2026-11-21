import React from "react";
import PropTypes from "prop-types";

export const SettingsEthernet = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_921)">
        <path
          d="M7.77031 6.75998L6.23031 5.47998L0.820312 12L6.23031 18.52L7.77031 17.24L3.42031 12L7.77031 6.75998ZM7.00031 13H9.00031V11H7.00031V13ZM17.0003 11H15.0003V13H17.0003V11ZM11.0003 13H13.0003V11H11.0003V13ZM17.7703 5.47998L16.2303 6.75998L20.5803 12L16.2303 17.24L17.7703 18.52L23.1803 12L17.7703 5.47998Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_921">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



SettingsEthernet.propTypes = {
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

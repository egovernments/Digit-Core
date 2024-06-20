import React from "react";
import PropTypes from "prop-types";

export const RemoveDone = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_831)">
        <path
          d="M1.79012 12.0001L7.37012 17.5901L5.96012 19.0001L0.370117 13.4101L1.79012 12.0001ZM2.24012 4.22006L12.9001 14.8901L11.6201 16.1701L7.44012 12.0001L6.03012 13.4101L11.6201 19.0001L14.3101 16.3101L19.2001 21.2001L20.6101 19.7901L3.65012 2.81006L2.24012 4.22006ZM17.1401 13.4901L23.6201 7.00006L22.2001 5.59006L15.7201 12.0701L17.1401 13.4901V13.4901ZM17.9601 7.00006L16.5501 5.59006L12.9001 9.25006L14.3101 10.6601L17.9601 7.00006Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_831">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



RemoveDone.propTypes = {
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

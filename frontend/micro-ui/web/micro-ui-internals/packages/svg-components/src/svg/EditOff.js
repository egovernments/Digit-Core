import React from "react";
import PropTypes from "prop-types";

export const EditOff = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_337)">
        <path
          d="M12.126 8.12506L14.063 6.18806L17.81 9.93506L15.873 11.8731L12.126 8.12506ZM20.71 5.63006L18.37 3.29006C18.1826 3.10381 17.9292 2.99927 17.665 2.99927C17.4008 2.99927 17.1474 3.10381 16.96 3.29006L15.13 5.12006L18.88 8.87006L20.71 7.00006C20.8844 6.8146 20.9815 6.56962 20.9815 6.31506C20.9815 6.0605 20.8844 5.81552 20.71 5.63006V5.63006ZM2 5.00006L8.63 11.6301L3 17.2501V21.0001H6.75L12.38 15.3801L18 21.0001L20 19.0001L4 3.00006L2 5.00006Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_337">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



EditOff.propTypes = {
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

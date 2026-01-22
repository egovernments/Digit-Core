import React from "react";
import PropTypes from "prop-types";

export const CellWifi = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_2023)">
        <path
          d="M17.9997 9.98006L5.99969 22.0001H17.9997H21.9997V5.97006L17.9997 9.98006ZM19.9997 20.0001H17.9997V12.7801L19.9997 10.7801V20.0001ZM5.21969 7.22006L3.92969 5.93006C7.82969 2.02006 14.1697 2.02006 18.0797 5.93006L16.7897 7.22006C13.5997 4.03006 8.40969 4.03006 5.21969 7.22006ZM12.9297 11.0701L10.9997 13.0001L9.06969 11.0701C10.1397 10.0101 11.8597 10.0101 12.9297 11.0701ZM14.2197 9.79006C12.4397 8.02006 9.55969 8.02006 7.78969 9.79006L6.49969 8.50006C8.97969 6.02006 13.0197 6.02006 15.4997 8.50006L14.2197 9.79006Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_2023">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



CellWifi.propTypes = {
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

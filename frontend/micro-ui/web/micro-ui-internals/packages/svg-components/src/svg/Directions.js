import React from "react";
import PropTypes from "prop-types";

export const Directions = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_10926)">
        <path
          d="M21.7103 11.2901L12.7103 2.29006C12.3203 1.90006 11.6903 1.90006 11.3003 2.29006L2.30031 11.2901C1.91031 11.6801 1.91031 12.3101 2.30031 12.7001L11.3003 21.7001C11.6903 22.0901 12.3203 22.0901 12.7103 21.7001L21.7103 12.7001C22.1003 12.3201 22.1003 11.6901 21.7103 11.2901ZM14.0003 14.5001V12.0001H10.0003V15.0001H8.00031V11.0001C8.00031 10.4501 8.45031 10.0001 9.00031 10.0001H14.0003V7.50006L17.5003 11.0001L14.0003 14.5001Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_10926">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



Directions.propTypes = {
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

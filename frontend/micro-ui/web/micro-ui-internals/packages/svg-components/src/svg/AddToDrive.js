import React from "react";
import PropTypes from "prop-types";

export const AddToDrive = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_48)">
        <path
          d="M7.71039 3.52L1.15039 15L4.57039 20.99L11.1304 9.52L7.71039 3.52V3.52ZM13.3504 15H9.73039L6.30039 21H14.5404C13.5804 19.94 13.0004 18.54 13.0004 17C13.0004 16.3 13.1304 15.63 13.3504 15ZM20.0004 16V13H18.0004V16H15.0004V18H18.0004V21H20.0004V18H23.0004V16H20.0004ZM20.7104 11.25L15.4204 2H8.58039V2.01L14.7304 12.78C15.8204 11.68 17.3304 11 19.0004 11C19.5904 11 20.1704 11.09 20.7104 11.25Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_48">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

AddToDrive.propTypes = {
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

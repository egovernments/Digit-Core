import React from "react";
import PropTypes from "prop-types";

export const DynamicForm = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_327)">
        <path
          d="M17 20V11H15V4H22L20 9H22L17 20ZM15 13V20H4C2.9 20 2 19.1 2 18V15C2 13.9 2.9 13 4 13H15ZM6.25 15.75H4.75V17.25H6.25V15.75ZM13 4V11H4C2.9 11 2 10.1 2 9V6C2 4.9 2.9 4 4 4H13ZM6.25 6.75H4.75V8.25H6.25V6.75Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_327">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



DynamicForm.propTypes = {
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

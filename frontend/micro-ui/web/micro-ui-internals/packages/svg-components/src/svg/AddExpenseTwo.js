import React from "react";
import PropTypes from "prop-types";

export const AddExpenseTwo = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_10096_28382)">
        <path
          d="M17.5996 11.75C18.2121 11.75 18.7984 11.8637 19.3496 12.0562V8.25L14.0996 3H5.34961C4.37836 3 3.59961 3.77875 3.59961 4.75V17C3.59961 17.9712 4.38711 18.75 5.34961 18.75H12.6559C12.4634 18.1987 12.3496 17.6125 12.3496 17C12.3496 14.1037 14.7034 11.75 17.5996 11.75ZM13.2246 4.3125L18.0371 9.125H13.2246V4.3125ZM21.0996 16.125V17.875H18.4746V20.5H16.7246V17.875H14.0996V16.125H16.7246V13.5H18.4746V16.125H21.0996Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_10096_28382">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

AddExpenseTwo.propTypes = {
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

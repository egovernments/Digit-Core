import React from "react";
import PropTypes from "prop-types";

export const NightlightRound = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_650)">
        <path
          d="M12.01 11.9998C12.01 8.42979 14.21 5.37979 17.32 4.12979C18.21 3.76979 18.07 2.43979 17.13 2.22979C16.03 1.98979 14.86 1.92979 13.65 2.08979C9.14003 2.68979 5.53003 6.39979 5.06003 10.9198C4.44003 16.9298 9.13003 21.9998 15.01 21.9998C15.74 21.9998 16.44 21.9198 17.13 21.7698C18.08 21.5598 18.23 20.2398 17.33 19.8698C14.11 18.5798 12 15.4598 12.01 11.9998V11.9998Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_650">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



NightlightRound.propTypes = {
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

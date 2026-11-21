import React from "react";
import PropTypes from "prop-types";

export const SearchOff = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_889)">
        <path
          d="M15.5003 14H14.7103L14.4303 13.73C15.4103 12.59 16.0003 11.11 16.0003 9.5C16.0003 5.91 13.0903 3 9.50027 3C6.08027 3 3.28027 5.64 3.03027 9H5.05027C5.30027 6.75 7.18027 5 9.50027 5C11.9903 5 14.0003 7.01 14.0003 9.5C14.0003 11.99 11.9903 14 9.50027 14C9.33027 14 9.17027 13.97 9.00027 13.95V15.97C9.17027 15.99 9.33027 16 9.50027 16C11.1103 16 12.5903 15.41 13.7303 14.43L14.0003 14.71V15.5L19.0003 20.49L20.4903 19L15.5003 14Z"
          fill={fill}
        />
        <path
          d="M6.47031 10.8198L4.00031 13.2898L1.53031 10.8198L0.820312 11.5298L3.29031 13.9998L0.820312 16.4698L1.53031 17.1798L4.00031 14.7098L6.47031 17.1798L7.18031 16.4698L4.71031 13.9998L7.18031 11.5298L6.47031 10.8198Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_889">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



SearchOff.propTypes = {
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

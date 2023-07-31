import React from "react";
import PropTypes from "prop-types";

export const Plumbing = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_1974_11328)">
        <path
          d="M19.28 4.9301L17.16 2.8101C16.38 2.0301 15.11 2.0301 14.33 2.8101L11.5 5.6401L13.62 7.7601L15.74 5.6401L19.28 9.1801C20.45 8.0001 20.45 6.1001 19.28 4.9301Z"
          fill={fill}
        />
        <path
          d="M5.48937 13.7699C6.07937 14.3599 7.02938 14.3599 7.60938 13.7699L10.0794 11.2999L7.95938 9.16992L5.48937 11.6399C4.89938 12.2299 4.89938 13.1799 5.48937 13.7699Z"
          fill={fill}
        />
        <path
          d="M15.0397 7.76009L14.3297 8.47009L13.6197 9.18009L10.4397 6.00009C9.84973 5.40009 8.89973 5.40009 8.31973 5.99009C7.72973 6.58009 7.72973 7.53009 8.31973 8.11009L11.4997 11.2901L10.7897 12.0001L4.42973 18.3601C3.64973 19.1401 3.64973 20.4101 4.42973 21.1901C5.20973 21.9701 6.47973 21.9701 7.25973 21.1901L16.4497 12.0001C16.8397 12.3901 17.4697 12.3901 17.8597 12.0001C18.2497 11.6101 18.2497 10.9801 17.8597 10.5901L15.0397 7.76009Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_1974_11328">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



Plumbing.propTypes = {
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

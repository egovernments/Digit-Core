import React from "react";
import PropTypes from "prop-types";

export const WorkOff = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_1233)">
        <path
          d="M23.0002 21.7402L21.5402 20.2802L7.21023 5.95023L3.25023 1.99023L1.99023 3.25023L4.69023 5.95023H4.05023C2.94023 5.95023 2.06023 6.84023 2.06023 7.95023L2.05023 18.9502C2.05023 20.0602 2.94023 20.9502 4.05023 20.9502H19.6902L21.7402 23.0002L23.0002 21.7402ZM22.0002 7.95023C22.0502 6.84023 21.1602 5.95023 20.0502 6.00023H16.0502V3.95023C16.0502 2.84023 15.1602 1.95023 14.0502 2.00023H10.0502C8.94023 1.95023 8.05023 2.84023 8.05023 3.95023V4.27023L22.0002 18.2702V7.95023ZM14.0502 6.00023H10.0002V3.95023H14.0502V6.00023Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_1233">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

WorkOff.propTypes = {
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

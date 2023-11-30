import React from "react";
import PropTypes from "prop-types";

export const SportsVolleyball = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_176_1064)">
        <path d="M6 4.00977C3.58 5.83977 2 8.72977 2 11.9998C2 13.4598 2.32 14.8498 2.89 16.1098L6 14.3098V4.00977Z" fill={fill} />
        <path d="M11 11.4198V2.0498C9.94 2.1598 8.93 2.4298 8 2.8398V13.1598L11 11.4198Z" fill={fill} />
        <path d="M11.9996 13.1499L3.88965 17.8299C4.49965 18.6699 5.22965 19.4199 6.06965 20.0299L14.9996 14.8899L11.9996 13.1499Z" fill={fill} />
        <path d="M13 7.95996V11.42L21.11 16.1C21.53 15.17 21.81 14.17 21.93 13.12L13 7.95996Z" fill={fill} />
        <path d="M8.07031 21.2C9.28031 21.71 10.6003 22 12.0003 22C15.3403 22 18.2903 20.35 20.1103 17.84L17.0003 16.04L8.07031 21.2Z" fill={fill} />
        <path d="M21.92 10.8098C21.37 6.1798 17.66 2.5098 13 2.0498V5.6498L21.92 10.8098Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_176_1064">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



SportsVolleyball.propTypes = {
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

import React from "react";
import PropTypes from "prop-types";

export const AllOut = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_73)">
        <path
          d="M16.21 4.15991L20.21 8.15991V4.15991H16.21ZM20.21 16.1599L16.21 20.1599H20.21V16.1599ZM8.20996 20.1599L4.20996 16.1599V20.1599H8.20996ZM4.20996 8.15991L8.20996 4.15991H4.20996V8.15991ZM17.16 7.20991C14.43 4.47991 9.98996 4.47991 7.25996 7.20991C4.52996 9.93991 4.52996 14.3799 7.25996 17.1099C9.98996 19.8399 14.43 19.8399 17.16 17.1099C19.89 14.3799 19.89 9.94991 17.16 7.20991V7.20991ZM16.06 16.0099C13.93 18.1399 10.49 18.1399 8.35996 16.0099C6.22996 13.8799 6.22996 10.4399 8.35996 8.30991C10.49 6.17991 13.93 6.17991 16.06 8.30991C18.19 10.4399 18.19 13.8799 16.06 16.0099Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_73">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

AllOut.propTypes = {
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

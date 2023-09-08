import React from "react";
import PropTypes from "prop-types";

export const PhoneDisabled = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_2193)">
        <path
          d="M17.3396 14.5401L15.9096 13.1101C16.4696 12.3801 16.9596 11.6101 17.3796 10.7901L15.1796 8.59006C14.8996 8.31006 14.8196 7.92006 14.9296 7.57006C15.2996 6.45006 15.4996 5.25006 15.4996 4.00006C15.4996 3.45006 15.9496 3.00006 16.4996 3.00006H19.9996C20.5496 3.00006 20.9996 3.45006 20.9996 4.00006C20.9996 7.98006 19.6296 11.6401 17.3396 14.5401ZM14.5196 17.3501C11.6296 19.6401 7.96965 21.0001 3.99965 21.0001C3.44965 21.0001 2.99965 20.5501 2.99965 20.0001V16.5101C2.99965 15.9601 3.44965 15.5101 3.99965 15.5101C5.23965 15.5101 6.44965 15.3101 7.56965 14.9401C7.91965 14.8201 8.31965 14.9101 8.58965 15.1801L10.7896 17.3801C11.5996 16.9601 12.3696 16.4801 13.0896 15.9201L1.38965 4.22006L2.80965 2.81006L21.1896 21.2001L19.7796 22.6101L14.5196 17.3501V17.3501Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_2193">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



PhoneDisabled.propTypes = {
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

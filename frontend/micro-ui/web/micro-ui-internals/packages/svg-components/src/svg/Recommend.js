import React from "react";
import PropTypes from "prop-types";

export const Recommend = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_176_867)">
        <path
          d="M12 2C10.0222 2 8.08879 2.58649 6.4443 3.6853C4.79981 4.78412 3.51809 6.3459 2.76121 8.17317C2.00433 10.0004 1.8063 12.0111 2.19215 13.9509C2.578 15.8907 3.53041 17.6725 4.92894 19.0711C6.32746 20.4696 8.10929 21.422 10.0491 21.8079C11.9889 22.1937 13.9996 21.9957 15.8268 21.2388C17.6541 20.4819 19.2159 19.2002 20.3147 17.5557C21.4135 15.9112 22 13.9778 22 12C22 10.6868 21.7413 9.38642 21.2388 8.17317C20.7363 6.95991 19.9997 5.85752 19.0711 4.92893C18.1425 4.00035 17.0401 3.26375 15.8268 2.7612C14.6136 2.25866 13.3132 2 12 2V2ZM18 11.8C18.0156 11.9728 17.9808 12.1465 17.9 12.3L15.8 17.2C15.6905 17.4498 15.5073 17.6602 15.2751 17.8032C15.0428 17.9462 14.7724 18.0148 14.5 18H9C8.46957 18 7.96086 17.7893 7.58579 17.4142C7.21072 17.0391 7 16.5304 7 16V11C6.99002 10.8143 7.02063 10.6286 7.08971 10.4559C7.15879 10.2832 7.26468 10.1276 7.4 10L12 5L12.69 5.69C12.8717 5.87916 12.9783 6.12798 12.99 6.39V6.59L12.41 10H17C17.2652 10 17.5196 10.1054 17.7071 10.2929C17.8946 10.4804 18 10.7348 18 11V11.8Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_176_867">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



Recommend.propTypes = {
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

import React from "react";
import PropTypes from "prop-types";

export const Construction = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_176_603)">
        <path d="M15.9035 13.0506L13.7822 15.1719L19.7784 21.1681L21.8997 19.0468L15.9035 13.0506Z" fill={fill} />
        <path
          d="M17.4996 10C19.4296 10 20.9996 8.43005 20.9996 6.50005C20.9996 5.92005 20.8396 5.38005 20.5896 4.90005L17.8896 7.60005L16.3996 6.11005L19.0996 3.41005C18.6196 3.16005 18.0796 3.00005 17.4996 3.00005C15.5696 3.00005 13.9996 4.57005 13.9996 6.50005C13.9996 6.91005 14.0796 7.30005 14.2096 7.66005L12.3596 9.51005L10.5796 7.73005L11.2896 7.02005L9.87961 5.61005L11.9996 3.49005C10.8296 2.32005 8.92961 2.32005 7.75961 3.49005L4.21961 7.03005L5.62961 8.44005H2.80961L2.09961 9.15005L5.63961 12.69L6.34961 11.98V9.15005L7.75961 10.56L8.46961 9.85005L10.2496 11.63L2.83961 19.0401L4.95961 21.16L16.3396 9.79005C16.6996 9.92005 17.0896 10 17.4996 10Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_176_603">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



Construction.propTypes = {
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

import React from "react";
import PropTypes from "prop-types";

export const Pets = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_755)">
        <path d="M4.5 12C5.88071 12 7 10.8807 7 9.5C7 8.11929 5.88071 7 4.5 7C3.11929 7 2 8.11929 2 9.5C2 10.8807 3.11929 12 4.5 12Z" fill={fill} />
        <path
          d="M9 8C10.3807 8 11.5 6.88071 11.5 5.5C11.5 4.11929 10.3807 3 9 3C7.61929 3 6.5 4.11929 6.5 5.5C6.5 6.88071 7.61929 8 9 8Z"
          fill={fill}
        />
        <path
          d="M15 8C16.3807 8 17.5 6.88071 17.5 5.5C17.5 4.11929 16.3807 3 15 3C13.6193 3 12.5 4.11929 12.5 5.5C12.5 6.88071 13.6193 8 15 8Z"
          fill={fill}
        />
        <path
          d="M19.5 12C20.8807 12 22 10.8807 22 9.5C22 8.11929 20.8807 7 19.5 7C18.1193 7 17 8.11929 17 9.5C17 10.8807 18.1193 12 19.5 12Z"
          fill={fill}
        />
        <path
          d="M17.3397 14.86C16.4697 13.84 15.7397 12.97 14.8597 11.95C14.3997 11.41 13.8097 10.87 13.1097 10.63C12.9997 10.59 12.8897 10.56 12.7797 10.54C12.5297 10.5 12.2597 10.5 11.9997 10.5C11.7397 10.5 11.4697 10.5 11.2097 10.55C11.0997 10.57 10.9897 10.6 10.8797 10.64C10.1797 10.88 9.59965 11.42 9.12965 11.96C8.25965 12.98 7.52966 13.85 6.64966 14.87C5.33966 16.18 3.72966 17.63 4.02966 19.66C4.31966 20.68 5.04966 21.69 6.35966 21.98C7.08966 22.13 9.41965 21.54 11.8997 21.54H12.0797C14.5597 21.54 16.8897 22.12 17.6197 21.98C18.9297 21.69 19.6597 20.67 19.9497 19.66C20.2597 17.62 18.6497 16.17 17.3397 14.86V14.86Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_755">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};



Pets.propTypes = {
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

import React from "react";
import PropTypes from "prop-types";

export const Torch = ({ className, style = {}, height = "38", width = "38", fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 38 38" fill="none" xmlns="http://www.w3.org/2000/svg">
      <path
        d="M23.6004 14H14.4004C14.2004 14 14.0004 14.2 14.0004 14.4V15.8C14.0004 15.9 14.1004 16 14.2004 16H23.8004C23.9004 16 24.0004 15.9 24.0004 15.8V14.4C24.0004 14.2 23.8004 14 23.6004 14Z"
        fill={fill}
      />
      <path
        d="M21.8004 32H16.2004C16.1004 32 16.0004 32.1 16.0004 32.2V33C16.0004 33.6 16.4004 34 17.0004 34H21.0004C21.6004 34 22.0004 33.6 22.0004 33V32.2C22.0004 32.1 21.9004 32 21.8004 32Z"
        fill={fill}
      />
      <path
        d="M23.2 17H14.8C14.7 17 14.6 17.2 14.6 17.3L16 19.9C16 20 16 20 16 20.1V30.8C16 30.9 16.1 31 16.2 31H21.8C21.9 31 22 30.9 22 30.8V20.1C22 20 22 20 22 19.9L23.3 17.3C23.4 17.2 23.3 17 23.2 17ZM19 23C18.4 23 18 22.6 18 22C18 21.4 18.4 21 19 21C19.6 21 20 21.4 20 22C20 22.6 19.6 23 19 23Z"
        fill={fill}
      />
      <path
        d="M20.0004 6C20.0004 5.44772 19.5526 5 19.0004 5C18.4481 5 18.0004 5.44772 18.0004 6V10C18.0004 10.5523 18.4481 11 19.0004 11C19.5526 11 20.0004 10.5523 20.0004 10V6Z"
        fill={fill}
      />
      <path
        d="M12.2324 6.86603C11.9563 6.38773 11.3447 6.22386 10.8664 6.5C10.3881 6.77614 10.2242 7.38773 10.5004 7.86603L12.5004 11.3301C12.7765 11.8084 13.3881 11.9723 13.8664 11.6962C14.3447 11.42 14.5085 10.8084 14.2324 10.3301L12.2324 6.86603Z"
        fill={fill}
      />
      <path
        d="M27.2324 7.86603C27.5085 7.38773 27.3447 6.77614 26.8664 6.5C26.3881 6.22386 25.7765 6.38773 25.5004 6.86603L23.5004 10.3301C23.2242 10.8084 23.3881 11.42 23.8664 11.6962C24.3447 11.9723 24.9563 11.8084 25.2324 11.3301L27.2324 7.86603Z"
        fill={fill}
      />
    </svg>
  );
};

Torch.propTypes = {
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

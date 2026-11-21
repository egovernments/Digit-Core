import React from "react";
import PropTypes from "prop-types";

export const Population = ({ className, width = "40", height = "46", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 40 46" fill="none" xmlns="http://www.w3.org/2000/svg">
      <path
        fill-rule="evenodd"
        clip-rule="evenodd"
        d="M19.9997 10C22.0699 10 23.7467 11.6356 23.7467 13.6544C23.7467 15.6735 22.0702 17.3088 19.9997 17.3088C17.9298 17.3088 16.2533 15.6737 16.2533 13.6544C16.2533 11.6356 17.9298 10 19.9997 10ZM10.3644 10C12.4346 10 14.1113 11.6356 14.1113 13.6544C14.1113 15.6735 12.4348 17.3088 10.3644 17.3088C8.29444 17.3088 6.61793 15.6737 6.61793 13.6544C6.61793 11.6356 8.29444 10 10.3644 10ZM14.178 27.0607V35.9994H6.55385V25.9281H4.5V22.5096C4.5 20.151 6.47417 18.2256 8.89249 18.2256H11.837C12.7563 18.2256 13.6145 18.5053 14.3214 18.9795C13.4815 19.9281 12.9739 21.1619 12.9739 22.5096V27.0602H14.178L14.178 27.0607ZM29.6354 10C31.7053 10 33.3818 11.6356 33.3818 13.6544C33.3818 15.6735 31.7053 17.3088 29.6354 17.3088C27.5652 17.3088 25.8884 15.6737 25.8884 13.6544C25.8884 11.6356 27.5649 10 29.6354 10ZM33.4484 25.9288V36H25.825V27.0613H27.0261V22.5107C27.0261 21.163 26.5182 19.9295 25.6786 18.9807C26.3855 18.5064 27.2437 18.2267 28.163 18.2267H31.1075C33.5258 18.2267 35.5 20.1522 35.5 22.5107V25.9293H33.4486L33.4484 25.9288ZM23.8131 25.9288V36H16.1896V25.9288H14.1352V22.5103C14.1352 20.1517 16.1094 18.2263 18.5277 18.2263H21.4722C23.8905 18.2263 25.8647 20.1517 25.8647 22.5103V25.9288H23.8131Z"
        fill={fill}
      />
    </svg>
  );
};



Population.propTypes = {
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

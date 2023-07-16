import React from "react";
import PropTypes from "prop-types";

export const UpdateExpenseSecondary = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <path
        d="M19.3463 12.07C19.4732 12.07 19.591 12.1243 19.6907 12.2238L20.8505 13.3819C21.0498 13.5719 21.0498 13.8886 20.8505 14.0786L19.9444 14.9833L18.0868 13.1286L18.993 12.2238C19.0926 12.1243 19.2195 12.07 19.3463 12.07ZM17.5613 13.6533L19.4188 15.5081L13.9278 21H12.0612V19.1362L17.5613 13.6533ZM17.4979 3.80952C18.4946 3.80952 19.3101 4.62381 19.3101 5.61905V9.2381L10.2489 18.2857V20.0952H4.81223C3.8155 20.0952 3 19.281 3 18.2857V5.61905C3 4.62381 3.8155 3.80952 4.81223 3.80952H8.5998C8.98037 2.76 9.97709 2 11.155 2C12.333 2 13.3297 2.76 13.7103 3.80952H17.4979ZM11.155 3.80952C10.6567 3.80952 10.2489 4.21667 10.2489 4.71429C10.2489 5.2119 10.6567 5.61905 11.155 5.61905C11.6534 5.61905 12.0612 5.2119 12.0612 4.71429C12.0612 4.21667 11.6534 3.80952 11.155 3.80952Z"
        fill={fill}
      />
    </svg>
  );
};

UpdateExpenseSecondary.propTypes = {
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

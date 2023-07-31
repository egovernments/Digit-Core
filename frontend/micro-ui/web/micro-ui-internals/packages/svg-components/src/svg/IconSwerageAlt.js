import React from "react";
import PropTypes from "prop-types";

export const IconSwerageAlt = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <path
        fill-rule="evenodd"
        clip-rule="evenodd"
        d="M7.01315 17.0306C6.8931 16.7772 6.63943 16.6168 6.36042 16.6168C6.08142 16.6168 5.82761 16.7772 5.70769 17.0306C5.70769 17.0306 4.40934 19.8411 4.40934 21.0409C4.40934 22.1223 5.28411 23 6.36055 23C7.43699 23 8.31176 22.1225 8.31176 21.0409C8.31176 19.8411 7.0134 17.0306 7.0134 17.0306H7.01315ZM13.0055 2.45147V4.28777H12.091C11.692 4.28777 11.3681 4.61229 11.3681 5.01361V6.23963H10.8627C10.4637 6.23963 10.1398 6.5648 10.1398 6.96547V7.36971H8.87039C6.18044 7.36971 4 9.5589 4 12.2598V14.6712C4 15.0725 4.32386 15.397 4.72292 15.397H7.99764C8.3967 15.397 8.72056 15.0725 8.72056 14.6712V12.9727C8.72056 12.7441 8.81092 12.5241 8.97213 12.3622C9.13333 12.2004 9.3523 12.1096 9.58016 12.1096H10.1398V12.5139C10.1398 12.9146 10.4636 13.2397 10.8627 13.2397H17.0984V13.8494C17.0984 14.2501 17.4216 14.5752 17.8213 14.5752H20.2771C20.6761 14.5752 21 14.2501 21 13.8494V5.21931C21 4.81799 20.6761 4.49347 20.2771 4.49347H17.8213C17.4216 4.49347 17.0984 4.81799 17.0984 5.21931V6.23985H16.0877V5.01384C16.0877 4.61252 15.7645 4.28799 15.3648 4.28799H14.451V2.45169H17.8213C18.2203 2.45169 18.5442 2.12652 18.5442 1.72584C18.5442 1.32517 18.2203 1 17.8213 1H9.63498C9.23592 1 8.91206 1.32517 8.91206 1.72584C8.91206 2.12652 9.23592 2.45169 9.63498 2.45169L13.0055 2.45147Z"
        fill={fill}
      />
    </svg>
  );
};



IconSwerageAlt.propTypes = {
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

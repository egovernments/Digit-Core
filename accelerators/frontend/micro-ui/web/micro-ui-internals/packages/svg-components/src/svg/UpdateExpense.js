import React from "react";
import PropTypes from "prop-types";

export const UpdateExpense = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_9765_27512)">
        <path
          d="M14.7895 16.3139H4.68421V4.73702H10.5789V2.84229H4.68421C3.75789 2.84229 3 3.69492 3 4.73702V16.1054C3 17.1475 3.75789 18.0002 4.68421 18.0002H14.7895C15.7158 18.0002 16.4737 17.1475 16.4737 16.1054V9.47386H14.7895V16.3139Z"
          fill={fill}
        />
        <path
          d="M16.4732 2.84234L14.789 2.84229L12.2627 2.84234C12.2711 2.85182 12.2627 4.73708 12.2627 4.73708H14.789V7.56971C14.7974 7.57918 16.4732 7.56971 16.4732 7.56971V4.73708V2.84234Z"
          fill={fill}
        />
        <path d="M13.105 6.63184H6.36816V8.52657H13.105V6.63184Z" fill={fill} />
        <path d="M6.36816 9.47363V11.3684H13.105V9.47363H10.5787H6.36816Z" fill={fill} />
        <path d="M13.105 12.3159H6.36816V14.2107H13.105V12.3159Z" fill={fill} />
        <g clip-path="url(#clip1_9765_27512)">
          <path
            d="M23.2733 12.8666L22.4667 12.0599C21.9467 11.5399 21.1 11.5399 20.58 12.0599L18.7933 13.8466L13 19.6399V22.3333H15.6933L21.52 16.5066L23.2733 14.7533C23.8 14.2333 23.8 13.3866 23.2733 12.8666V12.8666ZM15.14 20.9999H14.3333V20.1933L20.1067 14.4199L20.9133 15.2266L15.14 20.9999ZM18.3333 22.3333L21 19.6666H25V22.3333H18.3333Z"
            fill={fill}
          />
        </g>
      </g>
      <defs>
        <clipPath id="clip0_9765_27512">
          <rect width="24" height="24" fill="white" />
        </clipPath>
        <clipPath id="clip1_9765_27512">
          <rect width="16" height="16" fill="white" transform="translate(11 9)" />
        </clipPath>
      </defs>
    </svg>
  );
};

UpdateExpense.propTypes = {
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

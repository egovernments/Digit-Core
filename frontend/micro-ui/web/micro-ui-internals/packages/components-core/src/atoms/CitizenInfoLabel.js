import React from "react";
import { SVG } from "./SVG";
import PropTypes from "prop-types";

const CitizenInfoLabel = (props) => {
  props = props?.props ? props?.props : props;
  const showInfo = props?.showInfo ? props?.showInfo : true;

  return (
    <div className={`digit-info-banner-wrap ${props?.className ? props?.className : ""}`} style={props?.style}>
      {showInfo && (
        <div>
          <SVG.Info fill={props?.fill} />
          <h2 style={props?.textStyle}>{props?.info}</h2>
        </div>
      )}
      <p style={props?.textStyle}>{props?.text}</p>
    </div>
  );
};
CitizenInfoLabel.propTypes = {
  props: PropTypes.object, // An optional object of props.
  showInfo: PropTypes.bool, // An optional boolean to control the visibility of the info banner.
  className: PropTypes.string, // An optional string for custom class names.
  style: PropTypes.object, // An optional object for custom styles.
  fill: PropTypes.string, // An optional string for the icon fill color.
  textStyle: PropTypes.object, // An optional object for custom text styles.
  info: PropTypes.string, // An optional string for the info banner text.
  text: PropTypes.string.isRequired, // A required string for the main text content.
};

export default CitizenInfoLabel;

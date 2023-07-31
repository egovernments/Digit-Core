import React from "react";
import PropTypes from "prop-types";
import { SVG } from "./SVG";

const TelePhone = (props) => (
  <React.Fragment>
    {props?.text}
    <div className={`digit-telephone ${props?.className ? props?.className : ""}`} style={props?.style}>
      <div className={`digit-call`}>
        <SVG.Phone />
        <a href={`digit-tel:${props?.mobile}`}>
          {"+91"} {props?.mobile}
        </a>
      </div>
    </div>
  </React.Fragment>
);

TelePhone.propTypes = {
  text: PropTypes.string,
  mobile: PropTypes.string.isRequired,
  className: PropTypes.string,
  style: PropTypes.object,
};

export default TelePhone;

import React from "react";
import { SVG } from "./SVG";
import PropTypes from "prop-types";

const InfoBanner = ({ label, text }) => {
  return (
    <div className="digit-info-banner-wrap">
      <div>
        <SVG.Info />
        <h2>{label}</h2>
      </div>
      {text && <p>{text}</p>}
    </div>
  );
};

InfoBanner.propTypes = {
  label: PropTypes.string.isRequired,
  text: PropTypes.string,
};

InfoBanner.defaultProps = {
  label: "",
};

export default InfoBanner;

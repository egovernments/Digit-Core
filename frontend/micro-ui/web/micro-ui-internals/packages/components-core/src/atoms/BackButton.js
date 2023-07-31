import React from "react";
import { SVG } from "./SVG";
import { useTranslation } from "react-i18next";
import PropTypes from "prop-types";

const BackButton = ({ style, className = "", variant = "black", onClick }) => {
  const { t } = useTranslation();
  return (
    <div className={`digit-back-button ${className}`} style={style ? style : {}} onClick={onClick}>
      {variant == "black" ? (
        <React.Fragment>
          <SVG.ArrowLeft />
          <p>{t("CS_COMMON_BACK")}</p>
        </React.Fragment>
      ) : (
        <SVG.ArrowLeft />
      )}
    </div>
  );
};

BackButton.propTypes = {
  className: PropTypes.string,
  style: PropTypes.object,
  variant: PropTypes.string,
  onClick: PropTypes.func,
};

export default BackButton;

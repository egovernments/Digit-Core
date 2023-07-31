import React from "react";
import PropTypes from "prop-types";
import { useTranslation } from "react-i18next";
import { SVG } from "./SVG";

const NoResultsFound = (props) => {
  const { t } = useTranslation();
  const iconHeight = props?.height || 262;
  const iconWidth = props?.width || 336;
  return (
    <div className={`digit-no-data-found ${props?.className ? props?.className : ""}`} style={props?.style}>
      <SVG.NoResultsFoundIcon height={iconHeight} width={iconWidth} />
      <span className="digit-error-msg">{t("COMMON_NO_RESULTS_FOUND")}</span>
    </div>
  );
};
NoResultsFound.propTypes = {
  style: PropTypes.object,
  className: PropTypes.string,
  height: PropTypes.number, // Prop for the height of the NoResultsFoundIcon
  width: PropTypes.number, // Prop for the width of the NoResultsFoundIcon
};

// Default props for height and width
NoResultsFound.defaultProps = {
  height: 262,
  width: 336,
};
export default NoResultsFound;

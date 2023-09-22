import React, { useState } from "react";
import { useTranslation } from "react-i18next";
import { SVG } from "./SVG";
import PropTypes from "prop-types";

const CollapseAndExpandGroups = ({ children, groupElements = false, groupHeader = "", headerLabel = "", headerValue = "", customClass = "" ,showHelper=false,collapseState=false}) => {
  const { t } = useTranslation();
  const [collapse, setCollapse] = useState(collapseState);
  return (
    <div className={groupElements ? `digit-expand-collapse-wrapper ${customClass}` : `${customClass}`}>
      {groupHeader && <header style={{ marginBottom: "0px", fontSize: "24px" }}>{t(groupHeader)}</header>}
      {groupElements && (
        <div className="digit-expand-collapse-header">
          <span className="label">{headerLabel}</span>
          <span className="value">{headerValue}</span>
          <div onClick={() => setCollapse((prev) => !prev)} className="digit-icon-toggle ">
            {!collapse && (
              <span>
                {showHelper&&<h5>{t("COMMON_CORE_COLLAPSE")}</h5>}
                <SVG.ArrowDropUp height="40" width="40"/>
              </span>
            )}
            {collapse && (
              <span>                
                {showHelper&&<h5>{t("COMMON_CORE_EXPAND")}</h5>}
                <SVG.ArrowDropDown height="40" width="40"/>
              </span>
            )}
          </div>
        </div>
      )}
      <div className={`${groupElements ? "digit-toggling-wrapper" : ""} ${groupElements && collapse ? "digit-collapse" : ""}`}>{children}</div>
    </div>
  );
};

CollapseAndExpandGroups.propTypes = {
  children: PropTypes.node,
  style: PropTypes.object,
  customClass: PropTypes.string,
  groupElements: PropTypes.bool,
  groupHeader: PropTypes.string,
  headerLabel: PropTypes.string,
  headerValue: PropTypes.string,
};
export default CollapseAndExpandGroups;

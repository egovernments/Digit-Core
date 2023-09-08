import React, { useEffect } from "react";

import PopUp from "../atoms/PopUp";
import Button from "../atoms/Button";
import Toast from "../atoms/Toast";
import Header from "../atoms/Header";

const Modal = ({
  headerBarMain,
  headerBarEnd,
  popupStyles,
  children,
  actionCancelLabel,
  actionCancelOnSubmit,
  actionSaveLabel,
  actionSaveOnSubmit,
  error,
  setError,
  formId,
  isDisabled,
  hideSubmit,
  style = {},
  popupModuleMianStyles,
  headerBarMainStyle,
  isOBPSFlow = false,
  popupModuleActionBarStyles = {},
}) => {
  /**
   * TODO: It needs to be done from the desgin changes
   */
  const mobileView = Digit.Utils.browser.isMobile() ? true : false;
  useEffect(() => {
    document.body.style.overflowY = "hidden";
    return () => {
      document.body.style.overflowY = "auto";
    };
  }, []);
  return (
    <PopUp>
      <div className="popup-module" style={popupStyles}>
        {/* <HeaderBar main={headerBarMain} end={headerBarEnd} style={headerBarMainStyle ? headerBarMainStyle : {}} /> */}
        <Header className="header-wrap" styles={headerBarMainStyle ? headerBarMainStyle : {}}>
          {headerBarMain ? <div className="header-content">{headerBarMain}</div> : null}
          {headerBarEnd ? <div className="header-end">{headerBarEnd}</div> : null}
        </Header>
        <div className="popup-module-main" style={popupModuleMianStyles ? popupModuleMianStyles : {}}>
          {children}
          <div
            className="popup-module-action-bar"
            style={
              isOBPSFlow
                ? !mobileView
                  ? { marginRight: "18px" }
                  : { position: "absolute", bottom: "5%", right: "10%", left: window.location.href.includes("employee") ? "0%" : "7%" }
                : popupModuleActionBarStyles
            }
          >
            {actionCancelLabel ? (
              <Button textStyles={{ margin: "0px" }} theme="border" label={actionCancelLabel} onClick={actionCancelOnSubmit} style={style} />
            ) : null}
            {!hideSubmit ? (
              <Button
                textStyles={{ margin: "0px" }}
                label={actionSaveLabel}
                onClick={actionSaveOnSubmit}
                formId={formId}
                isDisabled={isDisabled}
                style={style}
              />
            ) : null}
          </div>
        </div>
      </div>
      {error && <Toast label={error} onClose={() => setError(null)} error />}
    </PopUp>
  );
};

export default Modal;

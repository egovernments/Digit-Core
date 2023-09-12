import React from "react";

function Footer(props) {
  const info = window?.Digit?.UserService?.getUser()?.info || null;
  const userType = info?.type || null;
  const additionalClassname = userType === "EMPLOYEE" ? "employee" : "citizen";

  return (
    <div className={`digit-home-footer ${additionalClassname} ${props?.className ? props?.className : ""}`} style={props?.style ? props?.style : {}}>
      <img
        className="digit-home-footer-img"
        alt="Powered by DIGIT"
        src={window?.globalConfigs?.getConfig?.("DIGIT_FOOTER")}
        onClick={() => {
          window.open(window?.globalConfigs?.getConfig?.("DIGIT_HOME_URL"), "_blank").focus();
        }}
      />
    </div>
  );
}

export default Footer;

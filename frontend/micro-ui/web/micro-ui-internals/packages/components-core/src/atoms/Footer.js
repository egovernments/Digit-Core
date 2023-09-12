import React from "react";

function Footer(props) {
  return (
    <div className={`digit-home-footer ${className}`} style={{ ...props.style }}>
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

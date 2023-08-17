import React, { Fragment } from "react";
import { SVG } from "./SVG";
import PropTypes from "prop-types";
import { useTranslation } from "react-i18next";

const CheckBox = ({ onChange, label, value, disable, ref, checked, inputRef, pageType, style, index, isLabelFirst, customLabelMarkup, ...props }) => {
  const { t } = useTranslation();
  const userType = pageType || window?.Digit?.SessionStorage.get("userType");
  let styles = props.styles;
  if (isLabelFirst) {
    return (
      <div className="digit-checkbox-wrap" style={styles ? styles : {}}>
        <p style={style ? style : null}> {index + 1}.</p>
        <p className="label" style={{ maxWidth: "80%", marginLeft: "10px" }}>
          {label}
        </p>
        <div>
          <input
            type="checkbox"
            className={userType === "employee" ? "input-emp" : ""}
            onChange={onChange}
            style={{ cursor: "pointer", left: "90%" }}
            value={value || label}
            {...props}
            ref={inputRef}
            disabled={disable}
            checked={checked}
          />
          <p
            className={
              userType === "employee" ? `digit-custom-checkbox-emp ${disable ? "disable" : ""}` : `digit-custom-checkbox ${disable ? "disable" : ""}`
            }
            style={disable ? { opacity: 0.5 } : { left: "90%" }}
          >
            <SVG.Check />
          </p>
        </div>
      </div>
    );
  } else {
    return (
      <div className="digit-checkbox-wrap" style={styles ? styles : {}}>
        <div>
          <input
            type="checkbox"
            className={userType === "employee" ? "input-emp" : ""}
            onChange={onChange}
            style={{ cursor: "pointer" }}
            value={value || label}
            {...props}
            ref={inputRef}
            disabled={disable}
            checked={checked}
          />
          <p
            className={
              userType === "employee" ? `digit-custom-checkbox-emp ${disable ? "disable" : ""}` : `digit-custom-checkbox ${disable ? "disable" : ""}`
            }
            style={disable ? { opacity: 0.5 } : null}
          >
            <SVG.Check />
          </p>
        </div>
        <p className="label" style={style ? style : {}}>
          {customLabelMarkup ? (
            <>
              <p>{t("COMMON_CERTIFY_ONE")}</p>
              <br />
              <p>
                <b> {t("ES_COMMON_NOTE")}</b>
                {t("COMMON_CERTIFY_TWO")}
              </p>
            </>
          ) : (
            label
          )}
        </p>
      </div>
    );
  }
};

CheckBox.propTypes = {
  /**
   * CheckBox content
   */
  label: PropTypes.string.isRequired,
  /**
   * onChange func
   */
  onChange: PropTypes.func,
  /**
   * input ref
   */
  ref: PropTypes.func,
  userType: PropTypes.string,
};

CheckBox.defaultProps = {
  label: "Default",
  isLabelFirst: true,
  onChange: () => console.log("CLICK"),
  value: "",
  checked: false,
  ref: "ww",
  // pageType: "EMPLOYEE",
  index: 0,
};

export default CheckBox;

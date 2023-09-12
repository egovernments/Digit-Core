import React from "react";
import PropTypes from "prop-types";
import { Link } from "react-router-dom";
import CitizenInfoLabel from "../atoms/CitizenInfoLabel";
import Button from "../atoms/Button";
import ActionBar from "../atoms/ActionBar";

export const Details = ({ label, name, onClick }) => {
  return (
    <div className="digit-detail" onClick={onClick}>
      <span className="digit-label">
        <h2>{label}</h2>
      </span>
      <span className="digit-name">{name}</span>
    </div>
  );
};

const DetailsCard = ({
  data,
  serviceRequestIdKey,
  linkPrefix,
  handleSelect,
  selectedItems,
  keyForSelected,
  handleDetailCardClick,
  isTwoDynamicPrefix = false,
  getRedirectionLink,
  handleClickEnabled = true,
  t,
  showActionBar = true,
  showCitizenInfoLabel = false,
  submitButtonLabel,
  styleVariant = "",
  redirectedLink,
  subRedirectedLink,
}) => {
  if (linkPrefix && serviceRequestIdKey) {
    return (
      <div>
        {data.map((object, itemIndex) => {
          return (
            <Link
              key={itemIndex}
              to={
                isTwoDynamicPrefix
                  ? `${linkPrefix}${
                      typeof serviceRequestIdKey === "function"
                        ? serviceRequestIdKey(object)
                        : // : `${getRedirectionLink(object["Application Type"] === "BPA_STAKEHOLDER_REGISTRATION" ? "BPAREG" : "BPA")}/${
                          `${getRedirectionLink(redirectedLink)}/${
                            // object[object["Application Type"] === "BPA_STAKEHOLDER_REGISTRATION" ? "applicationNo" : "Application Number"]
                            object[subRedirectedLink ? subRedirectedLink : "Application Number"]
                          }`
                    }`
                  : `${linkPrefix}${typeof serviceRequestIdKey === "function" ? serviceRequestIdKey(object) : object[serviceRequestIdKey]}`
              }
            >
              <div className="digit-details-container">
                {Object.keys(object).map((name, index) => {
                  if (name === "applicationNo" || name === "Vehicle Log") return null;
                  return <Details label={name} name={object[name]} key={index} />;
                })}
              </div>
            </Link>
          );
        })}
      </div>
    );
  }

  return (
    <div>
      {data.map((object, itemIndex) => {
        return (
          <div key={itemIndex} className={`digit-details-container ${styleVariant}`} onClick={() => handleClickEnabled && handleSelect(object)}>
            {Object.keys(object)
              .filter((rowEle) => !(typeof object[rowEle] == "object" && object[rowEle]?.hidden == true))
              .map((name, index) => {
                return <Details label={name} name={object[name]} key={index} onClick={() => handleClickEnabled && handleDetailCardClick(object)} />;
              })}
            {showCitizenInfoLabel ? (
              <CitizenInfoLabel className={"digit-core-variant"} info={t("ATM_INFO_LABEL")} text={t(`ATM_INFO_TEXT`)} fill={"#CC7B2F"} />
            ) : null}
            {showActionBar ? (
              <ActionBar>
                <Button onClick={() => handleDetailCardClick(object)} label={submitButtonLabel} />
              </ActionBar>
            ) : null}
          </div>
        );
      })}
    </div>
  );
};

DetailsCard.propTypes = {
  data: PropTypes.array,
};

DetailsCard.defaultProps = {
  data: [],
};

export default DetailsCard;

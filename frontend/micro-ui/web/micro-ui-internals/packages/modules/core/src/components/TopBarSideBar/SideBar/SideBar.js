import React, { useState } from "react";
import {
  ArrowForward,
  ArrowVectorDown,
  ArrowDirection,
  HomeIcon,
  ComplaintIcon,
  BPAHomeIcon,
  PropertyHouse,
  CaseIcon,
  ReceiptIcon,
  PersonIcon,
  DocumentIconSolid,
  DropIcon,
  CollectionsBookmarIcons,
  FinanceChartIcon,
  CollectionIcon,
} from "@egovernments/digit-ui-react-components";
import ReactTooltip from "react-tooltip";

const Sidebar = ({ data }) => {
  const [openItems, setOpenItems] = useState({});
  const getOrigin = window.location.origin;

  const IconsObject = {
    home: <HomeIcon />,
    announcement: <ComplaintIcon />,
    business: <BPAHomeIcon />,
    store: <PropertyHouse />,
    assignment: <CaseIcon />,
    receipt: <ReceiptIcon />,
    "business-center": <PersonIcon />,
    description: <DocumentIconSolid />,
    "water-tap": <DropIcon />,
    "collections-bookmark": <CollectionsBookmarIcons />,
    "insert-chart": <FinanceChartIcon />,
    edcr: <CollectionIcon />,
    collections: <CollectionIcon />,
  };

  const toggleSidebar = (key) => {
    setOpenItems((prevOpenItems) => ({
      ...prevOpenItems,
      [key]: !prevOpenItems[key],
    }));
  };

  const closeSidebar = () => {
    setOpenItems({});
  };

  function extractLeftIcon(data) {
    for (const key in data) {
      const item = data[key];

      if (key === "item" && item?.leftIcon !== "") {
        return item.leftIcon.split(":")[1];
      }

      if (typeof data[key] === "object" && !Array.isArray(data[key])) {
        const subResult = extractLeftIcon(data[key]);
        if (subResult) {
          return subResult; // Return as soon as a non-empty leftIcon is found
        }
      }
    }

    return null; // Return null if no non-empty leftIcon is found
  }

  const renderSidebarItems = (items, flag = true) => {
    return (
      <div className="submenu-container">
        {Object.keys(items).map((key, index) => {
          const subItems = items[key];
          const subItemKeys = Object.keys(subItems)[0] === "item";
          const isSubItemOpen = openItems[key] || false;
          if (!subItemKeys && subItems && Object.keys(subItems).length > 0) {
            // If the item has sub-items, render a dropdown with toggle button
            const leftIconArray = extractLeftIcon(subItems);
            let leftIcon = IconsObject[leftIconArray] || IconsObject.collections;
            return (
              <div key={index} className={`sidebar-link`} style={{ display: "flex", flexDirection: "column", alignItems: "flex-start" }}>
                <div className="actions" onClick={() => toggleSidebar(key)} style={{ display: "flex", flexDirection: "row" }}>
                  {flag && <div>{leftIcon}</div>}
                  <div data-tip="React-tooltip" data-for={`jk-side-${key}`}>
                    <span> {key} </span>
                    {key?.includes("...") && (
                      <ReactTooltip textColor="white" backgroundColor="grey" place="right" type="info" effect="solid" id={`jk-side-${key}`}>
                        {t(`ACTION_TEST_${key}`)}
                      </ReactTooltip>
                    )}
                  </div>
                  <div style={{ position: "absolute", right: "15px" }} className={`arrow ${isSubItemOpen ? "" : "hidden-arrow"}`}>
                    {isSubItemOpen ? <ArrowForward /> : <ArrowVectorDown />}
                  </div>
                </div>
                <div>{isSubItemOpen && renderSidebarItems(subItems, false)}</div>
              </div>
            );
          } else if (subItemKeys) {
            // If the item is a link, render it
            const leftIconArray = extractLeftIcon(subItems);
            let leftIcon = IconsObject[leftIconArray] || IconsObject.collections;
            return (
              <a key={index} className="dropdown-link new-dropdown-link" style={{ marginLeft: "0px" }}>
                <div className="actions" data-tip="React-tooltip" data-for={`jk-side-${index}`}>
                  {flag && <div style={{ display: isSubItemOpen ? "none" : "" }}>{leftIcon}</div>}
                  <div style={{ marginLeft: "20px" }}>{subItems.item.displayName}</div>
                </div>
              </a>
            );
          }
        })}
      </div>
    );
  };

  return (
    <div className={`new-sidebar ${openItems ? "show" : ""}`} onMouseLeave={closeSidebar}>
      {renderSidebarItems(data)}
    </div>
  );
};

export default Sidebar;

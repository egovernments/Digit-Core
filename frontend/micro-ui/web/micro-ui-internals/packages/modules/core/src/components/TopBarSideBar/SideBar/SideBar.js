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

  const renderSidebarItems = (items) => {
    return (
      <ul>
        {Object.keys(items).map((key, index) => {
          const subItems = items[key];
          const subItemKeys = Object.keys(subItems)[0] === "item";
          const isSubItemOpen = openItems[key] || false;
          if (!subItemKeys && subItems && Object.keys(subItems).length > 0) {
            // If the item has sub-items, render a dropdown with toggle button
            const leftIconArray = extractLeftIcon(subItems);
            console.log(leftIconArray);
            let leftIcon = IconsObject[leftIconArray] || IconsObject.collections;
            debugger;
            return (
              <div style={{ display: "flex" }}>
                <span style={{ marginLeft: "0px", marginRight: "0px" }}>{leftIcon}</span>
                <li key={index} className={`dropdown-toggle ${isSubItemOpen ? "open" : ""}`}>
                  <button onClick={() => toggleSidebar(key)} style={{ marginLeft: "0px" }}>
                    {isSubItemOpen} {key}
                  </button>
                  {isSubItemOpen && renderSidebarItems(subItems)}
                </li>
              </div>
            );
          } else if (subItemKeys) {
            // If the item is a link, render it
            return (
              <li key={subItems.item.id} className="actions">
                <a className="new-sidebar-link" href={getOrigin + "/employee/" + subItems.item.navigationURL}>
                  {subItems.item.displayName}
                </a>
              </li>
            );
          }
        })}
      </ul>
    );
  };

  return <div className={`new-sidebar ${openItems ? "show" : ""}`}>{renderSidebarItems(data)}</div>;
};

export default Sidebar;

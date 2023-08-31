import React, { useState } from "react";

const Sidebar = ({ data }) => {
  const [openItems, setOpenItems] = useState({});
  const getOrigin = window.location.origin;

  const toggleSidebar = (key) => {
    setOpenItems((prevOpenItems) => ({
      ...prevOpenItems,
      [key]: !prevOpenItems[key],
    }));
  };

  const renderSidebarItems = (items) => {
    return (
      <ul>
        {Object.keys(items).map((key, index) => {
          const subItems = items[key];
          const subItemKeys = Object.keys(subItems)[0] === "item";
          const isSubItemOpen = openItems[key] || false;

          if (!subItemKeys && subItems && Object.keys(subItems).length > 0) {
            // If the item has sub-items, render a dropdown with toggle button
            return (
              <li key={index} className={`dropdown-toggle ${isSubItemOpen ? "open" : ""}`}>
                <button onClick={() => toggleSidebar(key)} style={{ marginLeft: "0px" }}>
                  {isSubItemOpen} {key}
                </button>
                {isSubItemOpen && renderSidebarItems(subItems)}
              </li>
            );
          } else if (subItemKeys) {
            // If the item is a link, render it
            return (
              <li key={subItems.item.id}>
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

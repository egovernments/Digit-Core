import React, { useRef, useEffect, useState } from "react";
import { Loader, SearchIcon } from "@egovernments/digit-ui-react-components";
import { useTranslation } from "react-i18next";
import Sidebar from "./SideBar";

const checkMatch = (path = "", searchCriteria = "") => path.toLowerCase().includes(searchCriteria.toLowerCase());

const EmployeeSideBar = () => {
  const sidebarRef = useRef(null);
  const { isLoading, data } = Digit.Hooks.useAccessControl();
  const [search, setSearch] = useState("");
  const { t } = useTranslation();
  const [subNav, setSubNav] = useState(false);

  useEffect(() => {
    if (isLoading) {
      return <Loader />;
    }
    sidebarRef.current.style.cursor = "pointer";
    collapseNav();
  }, [isLoading]);

  const expandNav = () => {
    sidebarRef.current.style.width = "260px";
    // sidebarRef.current.style.overflow = "auto";
    setSubNav(true);
  };
  const collapseNav = () => {
    sidebarRef.current.style.width = "60px";
    sidebarRef.current.style.overflow = "hidden";
    setSubNav(false);
  };

  function mergeObjects(obj1, obj2) {
    for (const key in obj2) {
      if (obj2.hasOwnProperty(key)) {
        if (typeof obj2[key] === "object" && !Array.isArray(obj2[key])) {
          if (!obj1[key]) {
            obj1[key] = {};
          }
          mergeObjects(obj1[key], obj2[key]);
        } else {
          if (obj1[key]) {
            if (!Array.isArray(obj1[key])) {
              obj1[key] = [obj1[key]];
            }
            obj1[key].push(obj2[key]);
          } else {
            obj1[key] = obj2[key];
          }
        }
      }
    }
  }

  const configEmployeeSideBar = {};
  data?.actions
    .filter((e) => e.url === "url")
    .forEach((item) => {
      let index = item?.path?.split(".")?.[0] || "";
      if (search == "" && item.path !== "") {
        const keys = item.path.split(".");
        let hierarchicalMap = {};

        keys.reduce((acc, key, index) => {
          if (index === keys.length - 1) {
            // If it's the last key, set the value to an empty object or whatever you need.
            acc[key] = { item }; // You can set the value to any other value or object.
          } else {
            acc[key] = {};
            return acc[key]; // Return the nested object for the next iteration.
          }
        }, hierarchicalMap);
        mergeObjects(configEmployeeSideBar, hierarchicalMap);
      } else if (
        checkMatch(t(`ACTION_TEST_${index?.toUpperCase()?.replace(/[ -]/g, "_")}`), search) ||
        checkMatch(t(Digit.Utils.locale.getTransformedLocale(`ACTION_TEST_${item?.displayName}`)), search)
      ) {
        const keys = item.path.split(".");
        let hierarchicalMap = {};

        keys.reduce((acc, key, index) => {
          if (index === keys.length - 1) {
            // If it's the last key, set the value to an empty object or whatever you need.
            acc[key] = { item }; // You can set the value to any other value or object.
          } else {
            acc[key] = {};
            return acc[key]; // Return the nested object for the next iteration.
          }
        }, hierarchicalMap);
        mergeObjects(configEmployeeSideBar, hierarchicalMap);
      }
    });

  const splitKeyValue = (configEmployeeSideBar) => {
    const objectArray = Object.entries(configEmployeeSideBar);

    // Sort the array based on the 'orderNumber' or the length of the object if 'orderNumber' is not present
    objectArray.sort((a, b) => {
      const orderNumberA = a[1].item
        ? a[1].item.orderNumber || Object.keys(configEmployeeSideBar).length + 1
        : Object.keys(configEmployeeSideBar).length + 1;
      const orderNumberB = b[1].item
        ? b[1].item.orderNumber || Object.keys(configEmployeeSideBar).length + 1
        : Object.keys(configEmployeeSideBar).length + 1;
      return orderNumberA - orderNumberB;
    });
    const sortedObject = Object.fromEntries(objectArray);
    configEmployeeSideBar = sortedObject;
    return <Sidebar data={configEmployeeSideBar} />;
  };

  if (isLoading) {
    return <Loader />;
  }
  if (!configEmployeeSideBar) {
    return "";
  }

  const renderSearch = () => {
    return (
      <div className="">
        <div className="sidebar-link">
          {subNav ? (
            <div className="actions search-icon-wrapper">
              <input
                className="employee-search-input nav-bar"
                type="text"
                placeholder={t(`ACTION_TEST_SEARCH`)}
                name="search"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />
              <SearchIcon className="search-icon" />
            </div>
          ) : (
            <div className="actions search-icon-wrapper-new">
              <SearchIcon className="search-icon" />
            </div>
          )}
        </div>
      </div>
    );
  };

  return (
    <div className="sidebar" ref={sidebarRef} onMouseOver={expandNav} onMouseLeave={collapseNav}>
      {renderSearch()}
      {splitKeyValue(configEmployeeSideBar)}
    </div>
  );
};

export default EmployeeSideBar;

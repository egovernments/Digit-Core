import React from "react";
import PropTypes from "prop-types";
import { useLocation } from "react-router-dom";

const Card = ({ onClick, style = {}, children, className = "", ReactRef, ...props }) => {
  const { pathname } = useLocation();
  const classname = window?.Digit?.Hooks?.useRouteSubscription(pathname) || "";
  const info = window?.Digit?.UserService?.getUser()?.info || null;
  const userType = info?.type || null;
  const isEmployee = classname === "employee" || userType === "EMPLOYEE";

  return (
    <div
      className={`${props?.noCardStyle ? "" : isEmployee ? "digit-employee-card" : "digit-card"} ${className ? className : ""}`}
      onClick={onClick}
      style={style}
      {...props}
      ref={ReactRef}
    >
      {children}
    </div>
  );
};

Card.propTypes = {
  onClick: PropTypes.func,
  style: PropTypes.object,
  className: PropTypes.string,
  children: PropTypes.node,
};
export default Card;

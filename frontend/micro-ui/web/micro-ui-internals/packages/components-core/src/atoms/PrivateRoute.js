import React from "react";
import PropTypes from "prop-types";
import { Route, Redirect } from "react-router-dom";

export const PrivateRoute = ({ component: Component, roles, ...rest }) => {
  return (
    <Route
      {...rest}
      render={(props) => {
        const user = window?.Digit?.UserService.getUser();
        const userType = window?.Digit?.UserService.getType();
        function getLoginRedirectionLink() {
          if (userType === "employee") {
            return `/${window?.contextPath}/employee/user/language-selection`;
          } else {
            return `/${window?.contextPath}/citizen/login`;
          }
        }
        if (!user || !user.access_token) {
          // not logged in so redirect to login page with the return url
          return <Redirect to={{ pathname: getLoginRedirectionLink(), state: { from: props.location.pathname + props.location.search } }} />;
        }

        // logged in so return component
        return <Component {...props} />;
      }}
    />
  );
};

PrivateRoute.propTypes = {
  component: PropTypes.elementType.isRequired,
  roles: PropTypes.arrayOf(PropTypes.string),
};

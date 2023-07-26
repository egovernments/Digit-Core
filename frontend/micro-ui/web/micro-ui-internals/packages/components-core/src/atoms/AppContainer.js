import React from "react";
import PropTypes from "prop-types";

const AppContainer = (props) => {
  return (
    <React.Fragment>
      <div className={`digit-app-container ${props.className ? props.className : ""}`} style={props.style}>
        {props.children}
      </div>
    </React.Fragment>
  );
};

AppContainer.propTypes = {
  className: PropTypes.string,
  style: PropTypes.object,
  children: PropTypes.node,
};

export default AppContainer;

import React from "react";
import PropTypes from "prop-types";

const ErrorMessage = ({ message, className = "", style = {} }) => {
  return (
    <React.Fragment>
      <h2 className={`digit-error-message ${className}`} style={style}>
        {message}
      </h2>
    </React.Fragment>
  );
};

ErrorMessage.propTypes = {
  className: PropTypes.string,
  style: PropTypes.object,
  message: PropTypes.string,
};

export default ErrorMessage;

import React from "react";
import PropTypes from "prop-types";

export const LoaderScreen = ({ page = false }) => {
  return (
    <div className="digit-screen-loader">
      <div className="digit-loadingio-spinner-rolling-frame">
        <div className="digit-ldio-pjg92h09b2o">
          <div></div>
        </div>
      </div>
    </div>
  );
};

LoaderScreen.propTypes = {
  /**
   * Is this is page or a module?
   */
  page: PropTypes.bool,
};

LoaderScreen.defaultProps = {
  page: false,
};

import React from "react";
import PropTypes from "prop-types";

const HeaderBar = (props) => {
  return (
    <div className="digit-header-wrap" style={props?.style ? props.style : {}}>
      {props.start ? <div className="digit-header-start">{props.start}</div> : null}
      {props.main ? <div className="digit-header-content">{props.main}</div> : null}
      {props.end ? <div className="digit-header-end">{props.end}</div> : null}
    </div>
  );
};

HeaderBar.propTypes = {
  start: PropTypes.any,
  main: PropTypes.any,
  end: PropTypes.any,
};

HeaderBar.defaultProps = {
  start: "",
  main: "",
  end: "",
};

export default HeaderBar;

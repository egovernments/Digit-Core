import React from "react";
import PropTypes from "prop-types";

const Header = (props) => {
  return (
    <header className={`digit-header ${props?.className || ""}`} style={props?.styles || {}}>
      {props.children}
    </header>
  );
};

Header.propTypes = {
  className: PropTypes.string,
  styles: PropTypes.object,
  children: PropTypes.node,
};

export default Header;

import React from "react";

const Header = (props) => {
  return (
    <header className={`digit-header ${props?.className || ""}`} style={props?.styles || {}}>
      {props.children}
    </header>
  );
};

export default Header;

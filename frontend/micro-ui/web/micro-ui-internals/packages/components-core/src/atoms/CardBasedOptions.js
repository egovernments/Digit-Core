import React from "react";

const Option = ({ name, Icon, onClick, className }) => {
  return (
    <div className={className || `digit-card-based-options-main-child-option`} onClick={onClick}>
      <div className="digit-child-option-image-wrapper">{Icon}</div>
      <p className="digit-child-option-name">{name}</p>
    </div>
  );
};

const CardBasedOptions = ({ header, sideOption, options, styles = {}, style = {} }) => {
  return (
    <div className="digit-card-based-options" style={style}>
      <div className="digit-head-content">
        <h2>{header}</h2>
        <p onClick={sideOption.onClick}>{sideOption.name}</p>
      </div>
      <div className="digit-main-content">
        {options.map((props, index) => (
          <Option key={index} {...props} />
        ))}
      </div>
    </div>
  );
};

export default CardBasedOptions;

import React from "react";
import PropTypes from "prop-types";

const KeyNote = ({ keyValue, note, caption, noteStyle, children, privacy, className = "", style = {} }) => {
  return (
    <div className={`digit-key-note-pair ${className}`} style={style}>
      <h3>{keyValue}</h3>
      <div className="digit-key-note-container">
        <p style={noteStyle}>{note}</p>
      </div>
      <p className="digit-caption">{caption}</p>
      {children}
    </div>
  );
};

KeyNote.propTypes = {
  keyValue: PropTypes.string,
  note: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
  noteStyle: PropTypes.any,
};

KeyNote.defaultProps = {
  keyValue: "",
  note: "",
  noteStyle: {},
};

export default KeyNote;

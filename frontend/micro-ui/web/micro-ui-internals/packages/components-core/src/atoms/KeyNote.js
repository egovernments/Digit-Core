import React from "react";
import PropTypes from "prop-types";
// import { UnMaskComponent } from "..";
const KeyNote = ({ keyValue, note, caption, noteStyle, children, privacy, className = "", style = {} }) => {
  return (
    <div className={`digit-key-note-pair ${className}`} style={style}>
      <h3>{keyValue}</h3>
      <div className="digit-key-note-container">
        <p style={noteStyle}>{note}</p>
        {privacy && (
          <span className="digit-unmask-container">
            {/*  
                Feature :: Privacy
                privacy object set to the Mask Component
              */}
            {/* <UnMaskComponent privacy={privacy}></UnMaskComponent> */}
          </span>
        )}
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

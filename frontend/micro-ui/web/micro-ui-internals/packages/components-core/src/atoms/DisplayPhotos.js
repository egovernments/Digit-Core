import React from "react";
import PropTypes from "prop-types";
import { SVG } from "./SVG";
import getFileTypeFromFileStoreURL from "../utils/getFileTypeFromFileStoreURL";

const ImageOrPDFIcon = ({ source, index, last = false, onClick }) => {
  return getFileTypeFromFileStoreURL(source) === "pdf" ? (
    <div className="digit-image-pdf-icon">
      <a target="_blank" href={source} className="digit-url" key={index}>
        <div style={{ display: "flex", justifyContent: "center" }}>
          <SVG.Attachment className="digit-icon" width="100px" height="100px" />
        </div>
      </a>
    </div>
  ) : (
    <img key={index} src={source} {...(last ? { className: "last" } : {})} alt="issue thumbnail" onClick={() => onClick(source, index)}></img>
  );
};

const DisplayPhotos = (props) => {
  return (
    <div className={`digit-photos-wrap ${props?.className ? props?.className : ""}`} style={props?.style || {}}>
      {props.srcs.map((source, index) => {
        return <ImageOrPDFIcon key={index} {...{ source, index, ...props }} last={++index !== props.srcs.length ? false : true} />;
      })}
    </div>
  );
};

DisplayPhotos.propTypes = {
  /**
   * images
   */
  srcs: PropTypes.array,
  /**
   * optional click handler
   */
  onClick: PropTypes.func,
};

DisplayPhotos.defaultProps = {
  srcs: [],
};

export default DisplayPhotos;

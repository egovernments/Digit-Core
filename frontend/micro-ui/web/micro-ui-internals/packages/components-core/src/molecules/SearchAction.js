import React from "react";
import { SVG } from "../atoms/SVG";

const SearchAction = ({ text, handleActionClick }) => (
  <div className="digit-search-action" onClick={handleActionClick}>
    <SVG.Search /> <span className="digit-search-text">{text}</span>
  </div>
);

export default SearchAction;

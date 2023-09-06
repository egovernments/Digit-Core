import React from "react";
import { SVG } from "../atoms/SVG";

const SearchAction = ({ text, handleActionClick }) => (
  <div className="searchAction" onClick={handleActionClick}>
    <SVG.Search /> <span className="searchText">{text}</span>
  </div>
);

export default SearchAction;
